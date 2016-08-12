package org.openmrs.module.bahmniendtb.dataintegrity.service;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.ObsService;
import org.openmrs.module.episodes.Episode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataintegrityRuleService {

    private PatientProgramResultsMapper patientProgramResultsMapper;
    private SessionFactory sessionFactory;
    private ObsService obsService;

    @Autowired
    public DataintegrityRuleService(SessionFactory sessionFactory,
                                    PatientProgramResultsMapper patientProgramResultsMapper,
                                    ObsService obsService) {
        this.sessionFactory = sessionFactory;
        this.patientProgramResultsMapper = patientProgramResultsMapper;
        this.obsService = obsService;
    }

    public List<Episode> getEpisodeForEncountersWithDrugs(List<Concept> conceptsForDrugs) {
        StringBuilder queryString = new StringBuilder("select episode\n" +
                "from Episode as episode\n" +
                "    join episode.encounters as encounter\n" +
                "        join encounter.orders as order\n" +
                "    join episode.patientPrograms as patientProgram\n" +
                "where order.voided = false and order.action != 'DISCONTINUE'");
        if (CollectionUtils.isNotEmpty(conceptsForDrugs)) {
            queryString.append(" and order.concept in :conceptsForDrugs ");
        }
        Query query = sessionFactory.getCurrentSession().createQuery(queryString.toString());

        if (CollectionUtils.isNotEmpty(conceptsForDrugs)) {
            query.setParameterList("conceptsForDrugs", conceptsForDrugs);
        }
        return query.list();
    }

    public Set<Episode> filterEpisodesForObsWithSpecifiedValue(List<Episode> episodes, Concept questionConcept, List<Concept> valueCodedAnswers) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Episode.class, "episodes");
        criteria.createAlias("episodes.encounters", "encounters")
                .createAlias("encounters.obs", "parentObs")
                .add(Restrictions.in("episodes.episodeId", getEpisodeIds(episodes)))
                .add(Restrictions.eq("parentObs.concept", questionConcept))
                .add(Restrictions.eq("parentObs.voided", false))
                .add(Restrictions.not(Restrictions.in("parentObs.valueCoded", valueCodedAnswers)));

        List<Episode> consistentEpisodes = criteria.list();

        episodes.removeAll(consistentEpisodes);
        Set<Episode> filteredEpisodes = new HashSet<>();
        filteredEpisodes.addAll(episodes);
        return filteredEpisodes;
    }

    public List<Episode> getEpisodesWithRequiredObsValues(List<Episode> episodes, Concept questionConcept) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Episode.class, "episodes");
        criteria = criteria.createAlias("episodes.encounters", "encounters")
                .createAlias("encounters.obs", "parentObs");

        if (episodes != null && episodes.size() > 0)
            criteria = criteria.add(Restrictions.in("episodes.episodeId", getEpisodeIds(episodes)));

        criteria = criteria.add(Restrictions.eq("parentObs.concept", questionConcept))
                .add(Restrictions.eq("parentObs.voided", false))
                .add(Restrictions.not(Restrictions.isNotNull("parentObs.valueCoded")));

        List<Episode> consistentEpisodes = criteria.list();

        if (episodes != null && episodes.size() > 0)
            episodes.removeAll(consistentEpisodes);
        else
            episodes = consistentEpisodes;

        return episodes;
    }

    public Set<Episode> getUniqueEpisodeForEncountersWithConceptObs(List<Concept> conceptsForObs) {
        StringBuilder queryString = new StringBuilder("select episode\n" +
                "   from Episode as episode\n" +
                "   join episode.encounters as encounter\n" +
                "   join encounter.obs as obs\n" +
                "   where obs.voided = false");
        if(CollectionUtils.isNotEmpty(conceptsForObs)) {
            queryString.append("   and obs.concept in :conceptsForObs");
        }
        Query query = sessionFactory.getCurrentSession().createQuery(queryString.toString());

        if(CollectionUtils.isNotEmpty(conceptsForObs)) {
            query.setParameterList("conceptsForObs", conceptsForObs);
        }
        return new HashSet<>(query.list());
    }

    public List<Obs> getObsListForAPatient(Person whom, List<Encounter> encounters, List<Concept> questions) {
        return obsService.getObservations(Arrays.asList(whom), encounters, questions, null, null, null, null, null, null, null, null, false);
    }

    private List<Integer> getEpisodeIds(List<Episode> episodes) {
        List<Integer> episodeIds = new ArrayList<>();
        for (Episode episode : episodes) {
            episodeIds.add(episode.getEpisodeId());
        }

        return episodeIds;
    }
}

