/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sec2.saml.client.gui.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigInteger;
import org.sec2.managers.beans.User;

/**
 *
 * @author dev
 */
public class UserBean {

    private User user;
    public static final String PROP_USER = "user";
    private String email;
    public static final String PROP_EMAIL = "email";
    private String id;
    public static final String PROP_ID = "id";

    public UserBean() {
    }

    public UserBean(User user) {
        setUser(user);
    }

    /**
     * Get the value of id
     *
     * @return the value of id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the value of id
     *
     * @param id new value of id
     */
    private void setId(String id) {
        String oldId = this.id;
        this.id = id;
        propertyChangeSupport.firePropertyChange(PROP_ID, oldId, id);
    }

    /**
     * Get the value of email
     *
     * @return the value of email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the value of email
     *
     * @param email new value of email
     */
    private void setEmail(String email) {
        String oldEmail = this.email;
        this.email = email;
        propertyChangeSupport.firePropertyChange(PROP_EMAIL, oldEmail, email);
    }

    /**
     * Get the value of user
     *
     * @return the value of user
     */
    public User getUser() {
        return user;
    }

    /**
     * Set the value of user
     *
     * @param user new value of user
     */
    public void setUser(User user) {
        User oldUser = this.user;
        this.user = user;
        if (user != null) {
            setEmail(user.getEmailAddress());
            final BigInteger bi = new BigInteger(1, user.getUserID());
            final String id = String.format("%032X", bi);
            setId(id);
        }
        propertyChangeSupport.firePropertyChange(PROP_USER, oldUser, user);
    }
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}
