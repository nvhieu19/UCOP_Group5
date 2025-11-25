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
        try {
            ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
            if (pt != null && pt.getActualTypeArguments().length > 0) {
                this.persistenceClass = (Class<T>) pt.getActualTypeArguments()[0];
            } else {
                throw new RuntimeException("Could not extract generic type argument from " + getClass().getName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AbstractDAO for " + getClass().getName(), e);
        }
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
            if (persistenceClass == null) {
                throw new RuntimeException("persistenceClass is null. Failed to determine entity type for " + getClass().getName());
            }
            String hql = "FROM " + persistenceClass.getSimpleName();
            @SuppressWarnings("unchecked")
            Query<T> query = session.createQuery(hql);
            List<T> result = query.list();
            System.out.println("findAll() returned " + (result != null ? result.size() : 0) + " items from " + persistenceClass.getSimpleName());
            return result;
        } catch (Exception e) {
            System.err.println("Error in findAll() for " + (persistenceClass != null ? persistenceClass.getName() : "unknown class"));
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
}