package com.billhorvath.v1utils;

import static org.junit.Assert.*;
import org.junit.*;
import com.versionone.*;
import com.versionone.apiclient.services.*;
import com.versionone.apiclient.interfaces.*;

public class V1ServicesTest{
	@Test
	public void instantiatesCorrectly(){
		V1Services services = V1Services.getInstance();
		assertNotNull("Should not be null", services);
	}
	@Test
	public void returnsServices(){
		V1Services services = V1Services.getInstance();
		IServices iservices = services.services();
		assertNotNull("Should not be null", services);
	}
}