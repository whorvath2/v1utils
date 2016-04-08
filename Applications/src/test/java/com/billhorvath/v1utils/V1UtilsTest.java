package com.billhorvath.v1utils;

import static org.junit.Assert.*;
import org.junit.*;
import com.versionone.apiclient.filters.*;



public class V1UtilsTest{
	@Test
	public void nonExistentAssetTypeReturnsNull(){
		assertNull("Should be null", V1Utils.assetType("foobar"));
	}
	
	@Test
	public void knownAssetTypeButBadAttributeNameReturnsEmptyList(){

		assertTrue("List should be empty since attribute doesn't exist within a known good asset", V1Utils.findAssets("Story", "Foobar").size() == 0);
	}

	@Test
	public void goodOidsFoundAndBadOidsNotFound(){
		assertNull("Bad Oid should not be found", V1Utils.findAssetByOid("Foobar"));
		assertNotNull("Good Oid should be found", V1Utils.findAssetByOid("Scope:91791"));
	}
	//To-Do: Look for assets by good and bad Oids.
}
