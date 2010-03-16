/*
 * Copyright (c) 2010 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.umjammer03.lr;

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


/**
 * MixiFeintService.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 2010/02/14 nsano initial version <br>
 */
@Repository
@Transactional
public class MixiFeintService {

    @PersistenceContext
    EntityManager entityManager;

    public void createMixiFeint(MixiFeint mixiFeint) {
        entityManager.persist(mixiFeint);
    }

    public void deleteMixiFeint(Long id) {
        entityManager.remove(entityManager.find(MixiFeint.class, id));
    }

    public MixiFeint updateMixiFeint(MixiFeint MixiFeint) {
        return entityManager.merge(MixiFeint);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List getAllMixiFeints() {
        return entityManager.createQuery("SELECT i FROM MixiFeint i").getResultList();
    }

    @Transactional(readOnly = true)
    public MixiFeint getMixiFeint(Long id) {
        return entityManager.find(MixiFeint.class, id);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public MixiFeint getMixiFeint(String mixi) {

        Query query = entityManager.createQuery("SELECT i FROM MixiFeint i WHERE i.mixi = :mixi");
        query.setParameter("mixi", mixi);
        List results = query.getResultList();

        MixiFeint MixiFeint = null;
        Iterator it = results.iterator();
        if (it.hasNext()) {
            MixiFeint = (MixiFeint) it.next();
        }

        return MixiFeint;
    }
}

/* */
