package org.bahmni.flowsheet.definition.models;


import org.bahmni.flowsheet.api.Question;
import org.openmrs.Concept;
import org.openmrs.PatientProgram;

import java.util.Date;
import java.util.Set;

public class DrugQuestion extends Question {

    public DrugQuestion(Integer id, String name, Set<Concept> concepts, String value) {
        super(id,name,concepts, value);

    }

    @Override
    public void evaluate(PatientProgram patientProgram, Date startDate, Date endDate) {
            this.setValue("Pending");
    }
}
