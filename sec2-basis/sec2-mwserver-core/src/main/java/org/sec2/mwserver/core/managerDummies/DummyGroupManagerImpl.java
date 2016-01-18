/*
 * Copyright 2013 Ruhr-University Bochum, Chair for Network and Data Security
 *
 * This source code is part of the "Sec2" project and as this remains property
 * of the project partners. Content and concepts have to be treated as
 * CONFIDENTIAL. Publication or partly disclosure without explicit
 * written permission is prohibited.
 * For details on "Sec2" and its contributors visit
 *
 *        http://nds.rub.de/research/projects/sec2/
 */
package org.sec2.mwserver.core.managerDummies;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.sec2.exceptions.ExMiddlewareException;
import org.sec2.managers.IGroupManager;
import org.sec2.managers.IUserManager;
import org.sec2.managers.ManagerProvider;
import org.sec2.managers.beans.Group;
import org.sec2.managers.beans.User;
import org.sec2.securityprovider.exceptions.SecurityProviderException;
import org.sec2.securityprovider.mobileclient.MobileClientProvider;
import org.sec2.securityprovider.serviceparameter.PublicKeyType;
import org.sec2.securityprovider.serviceparameter.TokenType;
import org.sec2.token.TokenConstants;
import org.sec2.token.keys.GroupKey;
import org.spongycastle.jce.provider.BouncyCastleProvider;

/**
 * A Dummy Group Manager for testing and developing purposes.
 *
 * @author Dennis Felsch - dennis.felsch@rub.de
 * @version 0.1
 *
 * September 03, 2013
 */
public class DummyGroupManagerImpl implements IGroupManager, Serializable {

    private KeyStore keystore;
    private static File file;

    /**
     * ...
     */
    private DummyGroupManagerImpl() {
        createGroups();
    }
    private static DummyGroupManagerImpl instance = null;
    private static int groupId = 60;
    private LinkedList<Group> groups = null;
    LinkedList<User> users = null; //Prevent that the users get garbage collected

    public static DummyGroupManagerImpl getInstance(File pFile) {
        if (instance == null) {
            file = pFile;
            if (file.exists()) {
                instance = load();
            }
            if (instance == null) {
                instance = new DummyGroupManagerImpl();
            }
        }
        return instance;
    }

    /**
     * {@inheritDoc} Creates a new group.
     */
    @Override
    public void createGroup(final Group group)
            throws ExMiddlewareException, IOException {
        group.setSynced(true); //Dirty hack...It's ok here, because it's only a dummy
        groups.add(group);
        save();
    }

    /**
     * {@inheritDoc} Updates the passed group. The update is not persistent in
     * this dummy-implementation. As soon as this class is reloaded, all updates
     * done are reverted.
     */
    @Override
    public void updateGroup(final Group group)
            throws ExMiddlewareException, IOException {
        group.setSynced(true); //Dirty hack...It's ok here, because it's only a dummy
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getGroupName().equals(group.getGroupName())) {
                groups.remove(i);
                groups.add(i, group);
            }
        }
        save();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void revertGroupMembers(final Group group)
            throws ExMiddlewareException, IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ensureGroupKeyIsAvailable(String groupName)
            throws ExMiddlewareException, IOException {
        getGroup(groupName);
    }

    /**
     * {@inheritDoc} Removes the group with the passed group-ID. This change is
     * not persistent in this dummy-implementation. As soon as this class is
     * reloaded, all changes done are reverted.
     */
    @Override
    public void deleteGroup(final String groupName)
            throws ExMiddlewareException, IOException {
        for (int i = 0; i < groups.size(); i++) {
            if (groupName.equals(groups.get(i).getGroupName())) {
                groups.remove(i);
                break;
            }
        }
        save();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group getGroup(final String groupName)
            throws ExMiddlewareException, IOException {
        Group group = null;

        for (int i = 0; i < groups.size(); i++) {
            if (groupName.equals(groups.get(i).getGroupName())) {
                group = groups.get(i);
                break;
            }
        }
        return group;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getGroupsForUser(final byte[] userId)
            throws ExMiddlewareException, IOException {
        final List<String> memberships = new ArrayList<String>();
        Iterator<byte[]> iter = null;
        int pointer = 0;

        for (int i = 0; i < groups.size(); i++) {
            iter = groups.get(i).getMembers().iterator();
            while (iter.hasNext()) {
                final byte[] user = iter.next();
                if (Arrays.equals(user, userId)) {
                    memberships.add(groups.get(i).getGroupName());
                    break;
                }
            }
        }
        String[] result = new String[memberships.size()];
        return memberships.toArray(result);
    }

    private void createGroups() {
        try {
            this.setUpKeystore();

            final IUserManager um = ManagerProvider.getInstance().getUserManager();
            groups = new LinkedList<Group>();

            Group g = new Group("10000000", um.getRegisteredUser());
            g.addMember(um.getUser(new byte[]{11}));
            g.addMember(um.getUser(new byte[]{12}));
            g.setSynced(true); //dirrrty, harhar
            groups.add(g);

            GroupKey gKey;
            System.out.println("generating a group key");
            gKey = genGroupKey("10000000");
            System.out.println("group key generated");
            keystore.setKeyEntry(gKey.getId().toString(), gKey, null, null);

            g = new Group("20000000", um.getUser(new byte[]{20}));
            g.addMember(um.getUser(new byte[]{21}));
            g.addMember(um.getUser(new byte[]{22}));
            g.setSynced(true); //dirrrty, harhar
            groups.add(g);
            gKey = genGroupKey("20000000");
            keystore.setKeyEntry(gKey.getId().toString(), gKey, null, null);

            g = new Group("30000000", um.getUser(new byte[]{30}));
            g.addMember(um.getUser(new byte[]{31}));
            g.addMember(um.getUser(new byte[]{32}));
            g.setSynced(true); //dirrrty, harhar
            groups.add(g);
            gKey = genGroupKey("30000000");
            keystore.setKeyEntry(gKey.getId().toString(), gKey, null, null);

            g = new Group("40000000", um.getUser(new byte[]{40}));
            g.addMember(um.getUser(new byte[]{10}));
            g.addMember(um.getUser(new byte[]{11}));
            g.addMember(um.getUser(new byte[]{41}));
            g.addMember(um.getUser(new byte[]{42}));
            g.addMember(um.getUser(new byte[]{43}));
            g.setSynced(true); //dirrrty, harhar
            groups.add(g);
            gKey = genGroupKey("40000000");
            keystore.setKeyEntry(gKey.getId().toString(), gKey, null, null);

            g = new Group("Studium8", um.getRegisteredUser());
            g.setSynced(true); //dirrrty, harhar
            groups.add(g);
            gKey = genGroupKey("Studium8");
            keystore.setKeyEntry(gKey.getId().toString(), gKey, null, null);
        } catch (final Exception e) {
            e.printStackTrace(); //the most 1337 way to handle Exceptions ;)
        }
    }

    private void save() {
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            ObjectOutputStream o = new ObjectOutputStream(fos);
            o.writeObject(this);
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            try {
                fos.close();
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }
    }

    private static DummyGroupManagerImpl load() {
        InputStream fis = null;

        try {
            fis = new FileInputStream(file);
            ObjectInputStream o = new ObjectInputStream(fis);
            return (DummyGroupManagerImpl) o.readObject();
        } catch (IOException e) {
            System.err.println(e);
        } catch (ClassNotFoundException e) {
            System.err.println(e);
        } finally {
            try {
                fis.close();
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }
        return null;
    }

    private GroupKey genGroupKey(String id) {

        try {
            PublicKey publicKey = (PublicKey) keystore.getKey(
                    PublicKeyType.CLIENT_ENCRYPTION.name(), null);
            Cipher enc;
            byte[] key = new byte[TokenConstants.GKEY_LEN];
            byte[] kbytes = id.getBytes();
            for (int i = 0; i < kbytes.length; i++) {
                if (key.length > i) {
                    key[i] = kbytes[i];
                }
            }
            enc = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", new BouncyCastleProvider());
            enc.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encGroupKey = enc.doFinal(key);
            GroupKey groupKey = new GroupKey(encGroupKey, id.getBytes());
            return groupKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setUpKeystore() {
        try {
            MobileClientProvider.setType(TokenType.SOFTWARE_TOKEN);
            Security.insertProviderAt(
                    MobileClientProvider.getInstance(TokenConstants.DEFAULT_PIN), 1);
            Provider testProvider = MobileClientProvider.getInstance();
            if (keystore == null) {
                keystore = KeyStore.getInstance("Standard", testProvider);
            }
            keystore.load(null, null);
            clearKeyStore();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void clearKeyStore() {
        try {
            Enumeration aliases = keystore.aliases();
            LinkedList<String> aliasStrings = new LinkedList<String>();
            while (aliases.hasMoreElements()) {
                aliasStrings.add((String) aliases.nextElement());
            }
            for (String a : aliasStrings) {
                keystore.deleteEntry(a);
            }
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
    }
}
