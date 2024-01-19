import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.reuters.rfa.dacswebservice.*;

public class App {
	
	DacsAdministratorName dacsAdminName = null;
	DacsAdministratorPassword dacsAdminPassword = null;
	DacsAdministratorLogin dacsAdminLogin = null;
	DacsWebServiceService service = null;
	DacsWebService port = null;
	
	BufferedWriter writer = null;
	String dacs_username = "<DACS Admin>";
	String dacs_password = "<DACS Admin Password>";
	String dacs_wsdl = "http://<DACS IP Address>:8080/DacsWS/DacsWebServiceService?wsdl";
	URL dacs_url = null;
	
	public App() {
		
		try {
			dacs_url = new URL(dacs_wsdl);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dacsAdminName = new DacsAdministratorName();
		dacsAdminName.setMAdministratorName(dacs_username);
		
		dacsAdminPassword = new DacsAdministratorPassword();
		dacsAdminPassword.setMAdministratorPassword(dacs_password);
		
		dacsAdminLogin = new DacsAdministratorLogin();
		dacsAdminLogin.setAAdministratorName(dacsAdminName);
		dacsAdminLogin.setAAdministratorPassword(dacsAdminPassword);
		
		service = new com.reuters.rfa.dacswebservice.DacsWebServiceService(dacs_url);
		
		port = service.getDacsWebServicePort();	
		
	
		try {
			writer = new BufferedWriter(new FileWriter("dacs_permission.csv", false));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	public DacsVersionAttributes getVersion() {
		
	
		return port.getVersion(dacsAdminLogin);
		
	}
	
	public DacsSiteListResult getSiteList() {
		
		return port.getSiteList(dacsAdminLogin);
	}
	
	public DacsUserEntitlementsResult getDacsUserEntitlement(SiteName siteName, DacsUser user) {
		DacsEntitlementFilter filter = new DacsEntitlementFilter();
		filter.setMGetAllowed(true);
		filter.setMGetDenied(true);
		return port.getDacsUserEntitlements(dacsAdminLogin, siteName, user, filter);
	}
	public DacsUserListResult getDacsUserList(SiteName site) {
		return port.getDacsUserList(dacsAdminLogin, site, null);
	}
	
	public void writeToFile(String line) {
		try {
			writer.write(line);
			writer.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void printDacsSubServiceEntitlements(String siteName, String username,String service, String type, List<DacsSubserviceEntitlements> listSubServices) {
		
		
		for(DacsSubserviceEntitlements subService : listSubServices) {
			System.out.println(siteName+","+username+","+service+","+type+","+subService.getMName()+","+subService.isMAllowed());
		
			writeToFile(siteName+","+username+","+service+","+type+","+subService.getMName()+","+subService.isMAllowed());
				

			
		}
	}
	
	public void closeFile() {
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		
		
		
		App dacsApp = new App();
		
		DacsVersionAttributes dacsVersion = dacsApp.getVersion();
		
		System.out.println("DACS Version Result: "+ dacsVersion.getAResult().getMErrorText());
        System.out.println("Attributes: "+ dacsVersion.getAResult().getMResultText()+
                ", Version= "+ dacsVersion.getADacsDBVersion().getMDacsDatabaseVersion()+"\n");
        
        DacsSiteListResult siteList = dacsApp.getSiteList();
        
        System.out.println("DACS Site List Result: "+ siteList.getAResult().getMErrorText());
        System.out.println("DACSSite,User,Service,SubServiceType,SubServiceName,Allowed");
        dacsApp.writeToFile("DACSSite,User,Service,SubServiceType,SubServiceName,Allowed");
        for (SiteName siteName : siteList.getADacsSiteList()) {
        	
        	DacsUserListResult userList = dacsApp.getDacsUserList(siteName);
        	for (DacsUser user : userList.getADacsUserList()) {
        		
        		DacsUserEntitlementsResult entitlementResult = dacsApp.getDacsUserEntitlement(siteName, user);
        		
        		//System.out.println(entitlementResult.getAResult().getMResultText());
        		
        		for( DacsServiceEntitlements entitlement : entitlementResult.getADacsUserEntitlements().getMDacsServiceEntitlements()) {
        			
        			dacsApp.printDacsSubServiceEntitlements(siteName.getMSiteName(),user.getMDacsUser(),entitlement.getMName(),"Product", entitlement.getMDacsProductEntitlements());
        			dacsApp.printDacsSubServiceEntitlements(siteName.getMSiteName(),user.getMDacsUser(),entitlement.getMName(),"Exchange", entitlement.getMDacsExchangeEntitlements());
        			dacsApp.printDacsSubServiceEntitlements(siteName.getMSiteName(),user.getMDacsUser(),entitlement.getMName(),"Specialist", entitlement.getMDacsSpecialistEntitlements());
        			dacsApp.printDacsSubServiceEntitlements(siteName.getMSiteName(),user.getMDacsUser(),entitlement.getMName(),"QoS", entitlement.getMDacsQoSEntitlements());
        			
        			
        		}
        		
        		
        	}
        }
        
        dacsApp.closeFile();
	}
		

}