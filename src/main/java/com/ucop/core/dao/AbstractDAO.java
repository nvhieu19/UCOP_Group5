package com.ucop.core.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import com.ucop.util.HibernateUtil;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;



public abstract class AbstractDAO<T, ID extends Serializable> implements IGenericDAO<T, ID> {
    private Class<T> persistenceClass;

    @SuppressWarnings("unchecked")
    public AbstractDAO() {
        this.persistenceClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected Session getSession() {
        return HibernateUtil.getSessionFactory().openSession();
    }

    @Override
    public T save(T entity) {
        Session session = getSession();
        Transaction tr = null;
        try {
            tr = session.beginTransaction();
            session.save(entity);
            tr.commit();
            return entity;
        } catch (Exception e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    @Override
    public T update(T entity) {
        Session session = getSession();
        Transaction tr = null;
        try {
            tr = session.beginTransaction();
            session.update(entity);
            tr.commit();
            return entity;
        } catch (Exception e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }

    @Override
    public void delete(ID id) {
        Session session = getSession();
        Transaction tr = null;
        try {
            tr = session.beginTransaction();
            T entity = session.get(persistenceClass, id);
            if (entity != null) {
                session.delete(entity);
            }
            tr.commit();
        } catch (Exception e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    @Override
    public T findById(ID id) {
        try (Session session = getSession()) {
            return session.get(persistenceClass, id);
        }
    }

    @Override
    public List<T> findAll() {
        try (Session session = getSession()) {
            Query<T> query = session.createQuery("FROM " + persistenceClass.getName(), persistenceClass);
            return query.list();
        }
    }
}