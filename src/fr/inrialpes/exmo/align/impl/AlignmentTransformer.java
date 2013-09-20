package fr.inrialpes.exmo.align.impl;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import java.util.Collection;

/**
 * Transforms a URIAlignment to a ObjectAlignment
 */
public class AlignmentTransformer {


    public ObjectAlignment toObjectAlignment(URIAlignment al) throws AlignmentException {
        return toObjectAlignment0(al);
    }

    public ObjectAlignment toObjectAlignment0(URIAlignment al) throws AlignmentException {
        ObjectAlignment alignment = new ObjectAlignment();
        try {
            alignment.init(al.getFile1(), al.getFile2());
        } catch (AlignmentException aex) {
            try { // Really a friendly fallback
                alignment.init(al.getOntology1URI(), al.getOntology2URI());
            } catch (AlignmentException xx) {
                throw aex;
            }
        }
        alignment.setType(al.getType());
        alignment.setLevel(al.getLevel());
        alignment.setExtensions(al.convertExtension("ObjectURIConverted", "fr.inrialpes.exmo.align.ObjectAlignment#toObject"));
        LoadedOntology<Object> o1 = (LoadedOntology<Object>) alignment.getOntologyObject1(); // [W:unchecked]
        LoadedOntology<Object> o2 = (LoadedOntology<Object>) alignment.getOntologyObject2(); // [W:unchecked]
        Object obj1 = null;
        Object obj2 = null;

        try {
            for (Cell c : al) {
                try {
                    obj1 = o1.getEntity(c.getObject1AsURI(alignment));
                } catch (NullPointerException npe) {
                    throw new AlignmentException("Cannot dereference entity " + c.getObject1AsURI(alignment), npe);
                }
                try {
                    obj2 = o2.getEntity(c.getObject2AsURI(alignment));
                } catch (NullPointerException npe) {
                    throw new AlignmentException("Cannot dereference entity " + c.getObject2AsURI(alignment), npe);
                }
                //System.err.println( obj1+"  "+obj2+"  "+c.getRelation()+"  "+c.getStrength() );
                if (obj1 == null)
                    throw new AlignmentException("Cannot dereference entity " + c.getObject1AsURI(alignment));
                if (obj2 == null)
                    throw new AlignmentException("Cannot dereference entity " + c.getObject2AsURI(alignment));
                Cell newc = alignment.addAlignCell(c.getId(), obj1, obj2,
                        c.getRelation(), c.getStrength());
                Collection<String[]> exts = c.getExtensions();
                if (exts != null) {
                    for (String[] ext : exts) {
                        newc.setExtension(ext[0], ext[1], ext[2]);
                    }
                }
            }
        } catch (OntowrapException owex) {
            throw new AlignmentException("Cannot dereference entity", owex);
        }
        return alignment;
    }


}