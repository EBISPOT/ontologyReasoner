package uk.ac.ebi.fgpt.efo.utils;

import org.semanticweb.HermiT.Reasoner;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
//import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.io.*;

/**
 * Created by dwelter on 05/11/14.
 */
public class OntologyConfiguration {

    private OWLOntologyManager manager;
    private OWLDataFactory factory;


    public void setOntologyManager(){
        this.manager = OWLManager.createOWLOntologyManager();
    }

    public OWLOntologyManager getOntologyManager(){
        return manager;
    }

    public void setDataFactory(){
        this.factory = manager.getOWLDataFactory();
    }

    public OWLDataFactory getDataFactory(){
        return factory;
    }

    public OWLOntology loadOntology(String ontologyFile){

        OWLOntology ontology = null;
        try {
           ontology = manager.loadOntologyFromOntologyDocument(new FileInputStream(new File(ontologyFile)));
           System.out.println("Successfully loaded ontology " + ontologyFile);
        }
        catch (OWLOntologyCreationException e) {

            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ontology;

    }


    public OWLReasoner getReasoner(OWLOntology ontology){
            OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();


        //OWLReasonerFactory factory = new StructuralReasonerFactory();
            ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
            OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
            OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);
            return reasoner;


    }

    public void init(){
        System.setProperty("entityExpansionLimit", "100000000");

        setOntologyManager();
        setDataFactory();
    }


}
