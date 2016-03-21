import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHasher {
	
	public static boolean authenticate(String attemptedPassword, byte[] hashed, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
	  // Encrypt the clear-text password using the same salt that was used to
	  // encrypt the original password
	  byte[] pass = hashPass(attemptedPassword, salt);

	  for(int x = 0 ; x < pass.length; x++)
	  {
		  if(0 - Integer.valueOf(pass[x]) == 128)
			  pass[x] = 0;
		  if(pass[x] < 0)
			  pass[x] = Byte.valueOf(0 - Integer.valueOf(pass[x]) + "");
	  }
	  // Authentication succeeds if encrypted password that the user entered
	  // is equal to the stored hash  
	  return Arrays.toString(hashed).equals(Arrays.toString(pass));
	 }
	
	public static byte[] hashPass(String password, byte[] salt)
			   throws NoSuchAlgorithmException, InvalidKeySpecException {
		  // PBKDF2 with SHA-1 as the hashing algorithm. Note that the NIST
		  // specifically names SHA-1 as an acceptable hashing algorithm for PBKDF2
		  String algorithm = "PBKDF2WithHmacSHA1";
		  // SHA-1 generates 160 bit hashes, so that's what makes sense here
		  int derivedKeyLength = 160;
		  // Pick an iteration count that works for you. The NIST recommends at
		  // least 1,000 iterations:
		  // http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf
		  // iOS 4.x reportedly uses 10,000:
		  // http://blog.crackpassword.com/2010/09/smartphone-forensics-cracking-blackberry-backup-passwords/
		  int iterations = 20000;

		  KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);

		  SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);

		  byte[] pass = f.generateSecret(spec).getEncoded();

		  for(int x = 0 ; x < pass.length; x++)
		  {
			  if(0 - Integer.valueOf(pass[x]) == 128)
				  pass[x] = 0;
			  if(pass[x] < 0)
				  pass[x] = Byte.valueOf(0 - Integer.valueOf(pass[x]) + "");
		  }
		  return pass;
		 }
	
	public static byte[] getSalt() throws NoSuchAlgorithmException
	{
		  // VERY important to use SecureRandom instead of just Random
		  SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

		  // Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
		  byte[] salt = new byte[8];
		  random.nextBytes(salt);
		  for(int x = 0 ; x < 8; x++)
		  {
			  if(0 - Integer.valueOf(salt[x]) == 128)
				  salt[x] = 0;
			  if(salt[x] < 0)
				  salt[x] = Byte.valueOf(0 - Integer.valueOf(salt[x]) + "");
		  }
		  
		  return salt;
	}
}
