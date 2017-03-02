package webserver;

import java.util.HashMap;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import java.io.IOException;
import javax.xml.bind.DatatypeConverter;

public class Htpassword extends ConfigurationReader {
  private HashMap<String, String> passwords;

  public Htpassword( String filename ) throws IOException {
    super( filename );
    System.out.println( "Password file: " + filename );
    this.passwords = new HashMap<String, String>();
    this.load();
  }

  protected void parseLine( String line ) {
    String[] tokens = line.split( ":" );

    if( tokens.length == 2 ) {
      passwords.put( tokens[ 0 ], tokens[ 1 ].replace( "{SHA}", "" ).trim() );
    }
  }

  public boolean isAuthorized( String authInfo ) {
      String credentials = new String(
              DatatypeConverter.parseBase64Binary(authInfo),
              Charset.forName( "UTF-8" )
      );
      String[] tokens = credentials.split( ":" );
      return verifyPassword(tokens[0], tokens[1]);
  }

  private boolean verifyPassword( String username, String password ) {
    if (!passwords.containsKey(username)){
        return false;
    }
    
    String encryptedPassword = encryptClearPassword(password);
    
    if (passwords.get(username ).equals(encryptedPassword)){
        return true;
    } else {
        return false;
    }
  }

  private String encryptClearPassword( String password ) {
    try {
      MessageDigest mDigest = MessageDigest.getInstance( "SHA-1" );
      byte[] result = mDigest.digest( password.getBytes() );
      return DatatypeConverter.printBase64Binary(result);      
    } catch( Exception e ) {
      return "";
    }
  }

    @Override
    public void load() {
        while (hasMoreLines()){
            parseLine(nextLine()); 
        }
    }
}
