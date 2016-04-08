/**
com.billhorvath.v1utils is a collection of utilities and objects designed for use with the VersionOne API.

<h2>Usage Notes</h2>
<p>To connect to VersionOne, this package uses a VersionOne access token as the means by which it establishes authorization with VersionOne. The access token is in encrypted form in a plain-text file located at <span style="font-family: courier;">TokenUtils.TOKEN_FILE_LOC</span>, and the decryption key is in a file at <span style="font-family:courier;">EncryptionUtils.KEY_LOCATION</span>. <b>Currently, both files must be in the working directory from which the JAR file was launched in order for connections to the V1 server to be successful.</b> To create a new encrypted access token, use the Token Utilities from the command line to convert the token from plain text:</p> <div>java -jar [jarFileName] com.billhorvath.v1utils.TokenUtils [unencryptedToken]</div>
<p>Note that the key for encrypting and decrypting the token is stored in the <span style="font-family: courier;">EncryptionUtils.KEY_LOCATION</span> file. Neither the encrypted token nor the key are included in the source code repository for security reasons.</p>
*/
package com.billhorvath.v1utils;