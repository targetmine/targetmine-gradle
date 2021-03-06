package org.intermine.bio.postprocess;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intermine.metadata.ConstraintOp;
import org.intermine.metadata.Model;
import org.intermine.model.InterMineObject;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.query.Query;
import org.intermine.objectstore.query.QueryClass;
import org.intermine.objectstore.query.QueryField;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.objectstore.query.SimpleConstraint;
import org.intermine.postprocess.PostProcessor;

/**
 * Dump the primaryIdentifier to ncbiGeneId filed if it is empty.
 * 
 * @author chenyian
 *
 */
public class TranscribeNcbiGeneId extends PostProcessor {
	private static final Logger LOG = LogManager.getLogger(TranscribeNcbiGeneId.class);
	
	private Model model;

	public TranscribeNcbiGeneId(ObjectStoreWriter osw) {
		super(osw);
		model = Model.getInstanceByName("genomic");
	}
	
	public void transcribeIdentifeir() throws IllegalAccessException {
		Results results = getGenesWithoutNcbiGeneId();
		
		System.out.println(String.format("found %d genes with no ncbiGeneId", results.size()));
		LOG.info(String.format("found %d genes with no ncbiGeneId", results.size()));
		
		Iterator<?> iterator = results.iterator();
		
		try {
			osw.beginTransaction();

			while (iterator.hasNext()) {
				ResultsRow<?> result = (ResultsRow<?>) iterator.next();
				InterMineObject gene = (InterMineObject) result.get(0);
				gene.setFieldValue("ncbiGeneId", (String) gene.getFieldValue("primaryIdentifier"));
				osw.store(gene);
			}
			
			osw.commitTransaction();

		} catch (ObjectStoreException e) {
			e.printStackTrace();
		}
	}
	
	private Results getGenesWithoutNcbiGeneId() {
		Query q = new Query();
		QueryClass qcGene = new QueryClass(model.getClassDescriptorByName("Gene").getType());
		QueryField qfGeneId = new QueryField(qcGene, "ncbiGeneId");
		
		q.addFrom(qcGene);
		q.addToSelect(qcGene);
		q.setConstraint(new SimpleConstraint(qfGeneId,ConstraintOp.IS_EMPTY));
		
		ObjectStore os = osw.getObjectStore();

		return os.execute(q);
	}

	@Override
	public void postProcess() throws ObjectStoreException, IllegalAccessException {
		
		this.transcribeIdentifeir();
		
	}

}
