package org.openmrs.module.bahmniendtb.dataintegrity.rules;

import org.openmrs.module.dataintegrity.DataIntegrityRule;
import org.openmrs.module.dataintegrity.rule.RuleDefinition;
import org.openmrs.module.dataintegrity.rule.RuleResult;
import org.openmrs.Concept;
import org.openmrs.PatientProgram;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniendtb.dataintegrity.rules.helper.MissingValuesHelper;

import java.util.Arrays;
import java.util.List;

import static org.openmrs.module.bahmniendtb.EndTBConstants.*;

public class MissingOnsetDateSAEViolation implements RuleDefinition<PatientProgram> {

    private ConceptService conceptService;

        private MissingValuesHelper missingValuesHelper;

    public MissingOnsetDateSAEViolation(){
        conceptService = Context.getConceptService();
        missingValuesHelper = Context.getRegisteredComponent("missingValuesHelper",MissingValuesHelper.class);
    }

    public MissingOnsetDateSAEViolation(MissingValuesHelper missingDatesHelper, ConceptService conceptService) {
        this.missingValuesHelper = missingDatesHelper;
        this.conceptService = conceptService;
    }

    @Override
    public List<RuleResult<PatientProgram>> evaluate() {

        Concept reportingDateQuestion = conceptService.getConceptByName(SAE_REPORTING_DATE);
        Concept onsetDateQuestion = conceptService.getConceptByName(SAE_ONSET_DATE);

        return missingValuesHelper
            .getMissingObsInObsSetViolations(SAE_ADVERSE_EVENT_TEMPLATE, SAE_REPORTING_DATE,
                    Arrays.asList(reportingDateQuestion, onsetDateQuestion));
    }

    @Override
    public DataIntegrityRule getRule() {
        return null;
    }
}