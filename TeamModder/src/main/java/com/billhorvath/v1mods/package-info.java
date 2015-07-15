/**
v1mods is a collection of utilities and objects designed for use with the VersionOne API.

<h2>Usage Notes</h2>
<p>This package stores the access token for the VersionOne API in encrypted form in a plain-text file called <span style="font-family: courier;">enc_token</span>. To create a new encrypted access token, use the Token Utilities from the command line to convert the token from plain text:</p> <div>java -jar [jarFileName] com.billhorvath.v1mods.TokenUtils [unencryptedToken]</div>
<p>Note that the key for encrypting and decrypting the token is stored in the <span style="font-family: courier;">key</span> file. Neither enc_token nor key are included in the source code repository for security reasons; see Bill to get a copy of the current key and enc_token files, or a VersionOne admin to get new access tokens created.</p>
*/
package com.billhorvath.v1mods;