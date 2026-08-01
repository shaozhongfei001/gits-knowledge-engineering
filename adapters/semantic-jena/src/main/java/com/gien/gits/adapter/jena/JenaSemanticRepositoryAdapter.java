package com.gien.gits.adapter.jena;

import com.gien.gits.semantic.SemanticPackage;
import com.gien.gits.semantic.SemanticRepositoryPort;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;

public final class JenaSemanticRepositoryAdapter implements SemanticRepositoryPort {
    private final Dataset dataset = DatasetFactory.createTxnMem();
    private final AtomicReference<Model> shapes = new AtomicReference<>(ModelFactory.createDefaultModel());
    private final AtomicReference<String> version = new AtomicReference<>("NOT_LOADED");

    @Override
    public void load(SemanticPackage semanticPackage) {
        Objects.requireNonNull(semanticPackage, "semanticPackage");
        Model ontology = ModelFactory.createDefaultModel();
        RDFDataMgr.read(ontology, new ByteArrayInputStream(semanticPackage.ontologyTurtle()), Lang.TURTLE);
        Model shapeModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(shapeModel, new ByteArrayInputStream(semanticPackage.shaclTurtle()), Lang.TURTLE);
        dataset.begin(ReadWrite.WRITE);
        try {
            dataset.getDefaultModel().removeAll().add(ontology);
            dataset.commit();
            shapes.set(shapeModel);
            version.set(semanticPackage.version());
        } catch (RuntimeException error) {
            dataset.abort();
            throw error;
        } finally {
            dataset.end();
        }
    }

    @Override
    public ValidationResult validate(byte[] candidateTurtle) {
        if (candidateTurtle == null || candidateTurtle.length == 0) {
            return new ValidationResult(false, "candidate turtle is null or empty", version.get());
        }
        Model candidate = ModelFactory.createDefaultModel();
        RDFDataMgr.read(candidate, new ByteArrayInputStream(candidateTurtle), Lang.TURTLE);
        ValidationReport report = ShaclValidator.get().validate(shapes.get().getGraph(), candidate.getGraph());
        return new ValidationResult(report.conforms(), report.getModel().toString(), version.get());
    }
}
