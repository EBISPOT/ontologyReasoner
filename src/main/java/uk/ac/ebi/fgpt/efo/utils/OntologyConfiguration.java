package uk.ac.ebi.fgpt.efo.utils;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import java.io.File;

//import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

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
        OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();

        String ontologyDirectory = ontologyFile.substring(0, ontologyFile.lastIndexOf("/")+1);

        //Making ontology IRI to file path to make sure that all the imported ontologies will be found.
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        for (OntologyIRI2FilePathEnum enumeration : OntologyIRI2FilePathEnum.values()) {
            manager.addIRIMapper(
                    new SimpleIRIMapper(IRI.create(enumeration.getIri()),
                            IRI.create("file:" + ontologyDirectory + enumeration.getFileName())));
        }

        OWLOntology ontology = null;
        try {
            if(ontologyFile!=null) {

                ontology = manager.loadOntologyFromOntologyDocument(new FileDocumentSource(new File(ontologyFile)), config);
//                manager.setOntologyFormat(ontology, new RDFXMLOntologyFormat());
                // ... from named file
//            else if(file!=null)
//                ontology = manager.loadOntologyFromOntologyDocument(new FileDocumentSource(file), config);                // ... from file stream
//            else if(url!=null)
//                ontology = manager.loadOntologyFromOntologyDocument(new IRIDocumentSource(IRI.create(url)), config);     // ... from URL
            }else
                throw new RuntimeException("\nDefaultEFOReader.readOwl: All arguments are null!");
      //     ontology = manager.loadOntologyFromOntologyDocument(new FileInputStream(new File(ontologyFile)));
           System.out.println("Successfully loaded ontology " + ontologyFile);
        }
        catch (OWLOntologyCreationException e) {

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

//
//    // Load ontology
//    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

//
//    try
//    {
//        // Read EFO ...

//    }
//    catch



}
