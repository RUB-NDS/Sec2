package org.sec2.keyserver;

/**
 * @author Utimaco Safeware
 */
public interface IUserManagement {
		
		/** 
		 * This method creates a group.
		 *  
		 * @param  operatorId
		 *         Id of the user that creates the group.
		 * @param  groupName
		 *         Name of the group.
		 * @return If the creation of a group succeeds, an instance of IWrappedKey is returned 
		 *         that contains information about the created group and the wrapped groupKey.
		 *         Otherwise the returned IWrappedKey contains a error message. 
		 */
		public IWrappedKey createGroup(String operatorId, String groupName);
		
		
		/**
		 *  This method is used to confirm an email address.
		 * 
		 * @param  challenge
		 *         Signed challenge ([randomNumer+timestamp], SHA512withRSA).
		 * @param  timestamp 
		 *         Timestamp.
		 * @param  userPkc
		 *         PKC of the user (x509).
		 *         
		 * @return If the confirmation succeeds, an instance of IWrappedKey is returned 
		 *         that contains information about the created group and the wrapped groupKey.
		 *         Otherwise the returned IWrappedKey contains a error message.
		 * 
		 */
		public IWrappedKey emailConfirmation(byte[] challenge, long timestamp, byte[] userPKC);
		
		
		/** 
		 * This method returns the group key if the user specified by userId 
		 * is member of the group with the name groupName.
		 *  
		 * @param  userId 
		 *         Id of the user 
		 * @param  groupName 
		 *         Name of the group.
		 *         
         * @return If the retrieval of the key succeeds, an instance of IWrappedKey is returned 
		 *         that contains information about the group the key belongs to and the wrapped groupKey.
		 *         Otherwise the returned IWrappedKey contains a error message.
         */	
		public IWrappedKey getKey(String userId, String groupName);

		/**
		 * This method is the first step to register a new user to the KeyServer.
		 * 
		 * @param userPKC
		 *        PKC of the user.
		 * 
		 * @return true if the first registration step succeeds, false otherwise. 
		 */
		public boolean register(byte[] userPKC);
		
		/**
		 * This method signs the data and returns the signature of the data.
		 * 
		 * @param data
		 *        Data to be signed.
		 *         
		 * @return Signed data (SHA512withRSA).
		 *
		 */
		public byte[] sign(byte[]data);
		
		
		/**
		 *  This method validates a request via userId.
		 *   
		 * @param data
		 *        Data to be validated.
		 * @param signature
		 *        Signature (SHA512withRSA).
		 * @param userId
		 *        Id of the user.
		 *         
		 * @return true if validation succeeds, false otherwise.
		 */
		public boolean validate(byte data[], byte[] signature, String userId);
		
		
		/** 
		 * This method validates a request via PKC.
		 * 
		 * @param  data 
		 *         Data to be validated.
		 * @param  signature 
		 *         Signature (SHA512withRSA).
		 * @param  userPKC 
		 *         PKC of the user (x509).
		 *         
		 * @return true if validation succeeds, false otherwise. 
		 *         
		 */
		public boolean validate(byte[] data, byte[] signature, byte[] userPKC);

	}