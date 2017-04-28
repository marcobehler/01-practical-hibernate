import com.marcobehler.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * <pre>
 *                                                __   ._.
 *     ___.__. ____  __ __  _______  ____   ____ |  | _| |
 *    <   |  |/  _ \|  |  \ \_  __ \/  _ \_/ ___\|  |/ / |
 *     \___  (  <_> )  |  /  |  | \(  <_> )  \___|    < \|
 *     / ____|\____/|____/   |__|   \____/ \___  >__|_ \__
 *     \/                                      \/     \/\/
 *
 * </pre>
 */
public class DatabaseTest {

    private SessionFactory sessionFactory;

    private EntityManagerFactory entityManagerFactory;

    @Before
    public void setup() {
        entityManagerFactory = Persistence.createEntityManagerFactory("goHibernate!");
        sessionFactory = new Configuration().configure().buildSessionFactory(); // hibernate.cfg.xml
    }

    @Test
    public void test_openConnection() throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:")) {
            assertThat(connection.isValid(0), is(true));
        }
    }

    @Test
    public void test_openSession_with_hibernate() {
        try (Session session = sessionFactory.openSession()) {
            assertThat(session.isOpen(), is(true));
        }
    }

    // sql loggin', yay!

    @Test
    public void test_sql_logging() {
        try (Session session = sessionFactory.openSession()) {
            Integer id = (Integer) session.createNativeQuery("select 1 from dual").uniqueResult();
            assertThat(id, is(1));
        }
    }

    @Test
    public void test_save_user() {
        User user = new User();
        user.setEmail("hans@dampf.com");
        user.setPassword("s3cr3t");

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(user);
            tx.commit();
        }

        assertThat(user.getId(), is(notNullValue()));
        System.out.println("Our user got a new database id[=" + user.getId() + "]");
    }



    @Test
    public void test_delete_user() {
        User user = new User();
        user.setEmail("some@user.com");
        user.setPassword("s3cr3t");

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(user);
            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

          //  session.delete(user);
            session.createQuery("delete from User where id = :id").setParameter("id", user.getId()).executeUpdate();
            tx.commit();
        }
    }

    @Test
    public void test_load_and_update_user() {
        User user = new User();
        user.setEmail("tywin@lannister.com");
        user.setPassword("s3cr3t");

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.save(user);
            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            // load user..with HQL!
            User tywin = (User) session.createQuery("from User where email = :email").setParameter("email", "tywin@lannister.com").uniqueResult();
            // update his email
            tywin.setEmail("tywin@paidhisdebts.com");
            session.update(tywin);
            tx.commit();
        }

        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();

            User oldTywin = (User) session.createQuery("from User where email = :email").setParameter("email", "tywin@lannister.com").uniqueResult();
            User newTywin = (User) session.createQuery("from User where email = :email").setParameter("email", "tywin@paidhisdebts.com").uniqueResult();
            assertThat(oldTywin, is(nullValue()));
            assertThat(newTywin, is(notNullValue()));


            tx.commit();
        }
    }

    @Test
    public void test_save_user_with_jpa() {
        User user = new User();
        user.setEmail("hans@dampf.com");
        user.setPassword("s3cr3t");

        EntityManager manager = entityManagerFactory.createEntityManager();

        EntityTransaction tx = manager.getTransaction();
        tx.begin();
        manager.persist(user);
        tx.commit();
        manager.close();

        assertThat(user.getId(), is(notNullValue()));
        System.out.println("Our user got a new database id[=" + user.getId() + "]");
    }

}

