package org.openmrs.module.bahmniendtb.dataintegrity.rules;

import org.openmrs.module.bahmniendtb.dataintegrity.rules.helper.MissingCaseDefnHelper;
import org.openmrs.module.dataintegrity.rule.RuleResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.PatientProgram;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniendtb.EndTBConstants;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openmrs.module.bahmniendtb.EndTBConstants.*;

public class MissingCaseDefinitionViolationTest {

    @Mock
    ConceptService conceptService;

    @Mock
    MissingCaseDefnHelper missingCaseDefnHelper;

    @InjectMocks
    MissingCaseDefinitionViolation missingCaseDefinitionViolation;

    @Mock
    Concept whoGroupConcept;

    @Mock
    Concept diseaseSiteConcept;

    @Mock
    Concept confirmationMethodConcept;

    @Before
    public void setUp(){
        initMocks(this);
    }

    @Test
    public void ensureMissingEventBecameSeriousViolationsAreReturned() throws Exception {
        List<RuleResult<PatientProgram>> outputMock = new ArrayList<>();
        outputMock.add(new RuleResult<PatientProgram>());

        when(missingCaseDefnHelper
                .getMissingObsInObsSetViolations(any(String.class), any(List.class)))
                .thenReturn(outputMock);

        when(conceptService.getConceptByName(EndTBConstants.BASELINE_CASEDEFINITION_WHO_GROUP)).thenReturn(diseaseSiteConcept);
        when(conceptService.getConceptByName(EndTBConstants.BASELINE_CASEDEFINITION_DISEASE_SITE)).thenReturn(diseaseSiteConcept);
        when(conceptService.getConceptByName(EndTBConstants.BASELINE_CASEDEFINITION_CONFIRMATION)).thenReturn(confirmationMethodConcept);


        List<RuleResult<PatientProgram>> result = missingCaseDefinitionViolation.evaluate();

        ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        verify(missingCaseDefnHelper)
                .getMissingObsInObsSetViolations(   eq(BASELINE_FORM),
                                                    argument.capture());
        assertEquals(3, argument.getValue().size());
    }

}