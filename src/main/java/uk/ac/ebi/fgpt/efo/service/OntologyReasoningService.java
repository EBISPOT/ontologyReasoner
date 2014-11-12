package uk.ac.ebi.fgpt.efo.service;


import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.efo.exception.OWLConversionException;
import uk.ac.ebi.fgpt.efo.utils.OntologyConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by dwelter on 05/11/14.
 */
public class OntologyReasoningService {

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    private OntologyConfiguration ontologyConfiguration;

    public void setOntologyConfiguration(OntologyConfiguration ontologyConfiguration) {
        this.ontologyConfiguration = ontologyConfiguration;
    }

    public OWLOntologyManager getManager() {
        return ontologyConfiguration.getOntologyManager();
    }


    public OWLReasoner reasonOverOntology(String ontologyFile) throws OWLConversionException {

        System.out.println("Attempting to load ontology file...");
        OWLOntology ontology = ontologyConfiguration.loadOntology(ontologyFile);

        getLog().debug("Creating reasoner... ");

        OWLReasoner reasoner = ontologyConfiguration.getReasoner(ontology);

        getLog().debug("Precomputing inferences...");
        reasoner.precomputeInferences();

        getLog().debug("Checking ontology consistency...");
        reasoner.isConsistent();

        getLog().debug("Checking for unsatisfiable classes...");

        if (reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom().size() > 0) {
            throw new OWLConversionException("Once classified, unsatisfiable classes were detected");
        }
        else {
            getLog().info("Reasoning complete! ");
            return reasoner;
        }


    }


    public void saveInferredOntology(OWLReasoner reasoner, String outputFile) throws OWLConversionException {
        try {
            // create new ontology to hold inferred axioms
            System.out.println("Adding the required axioms");
           getLog().info("Saving inferred view...");
            List<InferredAxiomGenerator<? extends OWLAxiom>> gens =new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
            // we require all inferred stuff except for disjoints...
            gens.add(new InferredClassAssertionAxiomGenerator());
            gens.add(new InferredDataPropertyCharacteristicAxiomGenerator());
            gens.add(new InferredEquivalentClassAxiomGenerator());
            gens.add(new InferredEquivalentDataPropertiesAxiomGenerator());
            gens.add(new InferredEquivalentObjectPropertyAxiomGenerator());
            gens.add(new InferredInverseObjectPropertiesAxiomGenerator());
            gens.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
            gens.add(new InferredPropertyAssertionGenerator());
            gens.add(new InferredSubClassAxiomGenerator());
            gens.add(new InferredSubDataPropertyAxiomGenerator());
            gens.add(new InferredSubObjectPropertyAxiomGenerator());

            System.out.println("Axiom generator complete");

            OWLOntology inferredOntology = stripOriginalOntology(reasoner);

            InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, gens);
            System.out.println("Inferred axioms passed to the reasoner");
            iog.fillOntology(getManager(), inferredOntology);
            System.out.println("Ontology filled");
            getManager().saveOntology(inferredOntology, new FileOutputStream(new File(outputFile)));
            getLog().info("Inferred view saved ok");
        }
        catch (OWLOntologyStorageException e) {
            e.printStackTrace();
            throw new OWLConversionException("Failed to save inferred ontology view",e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public OWLOntology stripOriginalOntology(OWLReasoner reasoner){

        OWLOntology ontology = reasoner.getRootOntology();

        Set<OWLAxiom> allAxioms = ontology.getAxioms();

        for(OWLAxiom axiom : allAxioms){
            if(axiom.isOfType(AxiomType.SUBCLASS_OF) || axiom.isOfType(AxiomType.EQUIVALENT_CLASSES) || axiom.isOfType(AxiomType.DISJOINT_CLASSES)){
                System.out.println(axiom.toString());

                getManager().removeAxiom(ontology, axiom);
            }
        }

        return ontology;
    }
}
