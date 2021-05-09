package af.aib.paymentResponse.util;

import java.io.File;

import af.aib.paymentResponse.config.AppConfig;
import af.aib.paymentResponse.log.ActivityLogger;

public class Banner {

	private String softwareName, SoftwareVersion, ownerCompany, developerName, developerEmail, poweredBy;

	private static String bannerFileName = "appBanner.txt";
	private static ClassLoader bannerClassLoader = null;

	static AppConfig config = new AppConfig();

	private static String loggMsg = "";
	private static File file = null;

	public Banner() {
		super();
	}

	public Banner(String softwareName, String softwareVersion, String ownerCompany, String developerName,
			String developerEmail, String poweredBy) {
		super();
		this.softwareName = softwareName;
		SoftwareVersion = softwareVersion;
		this.ownerCompany = ownerCompany;
		this.developerName = developerName;
		this.developerEmail = developerEmail;
		this.poweredBy = poweredBy;
	}

	public String getSoftwareName() {
		return softwareName;
	}

	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
	}

	public String getSoftwareVersion() {
		return SoftwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		SoftwareVersion = softwareVersion;
	}

	public String getOwnerCompany() {
		return ownerCompany;
	}

	public void setOwnerCompany(String ownerCompany) {
		this.ownerCompany = ownerCompany;
	}

	public String getDeveloperName() {
		return developerName;
	}

	public void setDeveloperName(String developerName) {
		this.developerName = developerName;
	}

	public String getDeveloperEmail() {
		return developerEmail;
	}

	public void setDeveloperEmail(String developerEmail) {
		this.developerEmail = developerEmail;
	}

	public String getPoweredBy() {
		return poweredBy;
	}

	public void setPoweredBy(String poweredBy) {
		this.poweredBy = poweredBy;
	}

	public static String getBannerFileName() {
		return bannerFileName;
	}

	public static void setBannerFileName(String bannerFileName) {
		Banner.bannerFileName = bannerFileName;
	}

	public static ClassLoader getBannerClassLoader() {
		return bannerClassLoader;
	}

	public static void setBannerClassLoader(ClassLoader bannerClassLoader) {
		Banner.bannerClassLoader = bannerClassLoader;
	}

	public static AppConfig getConfig() {
		return config;
	}

	public static void setConfig(AppConfig config) {
		Banner.config = config;
	}

	public static String getLoggMsg() {
		return loggMsg;
	}

	public static void setLoggMsg(String loggMsg) {
		Banner.loggMsg = loggMsg;
	}

	public static File getFile() {
		return file;
	}

	public static void setFile(File file) {
		Banner.file = file;
	}

	public static void printBanner() {
		file = new File(AppConfig.getAppBannerPath() + "appBanner.txt");
		loggMsg = AppCommons.readFileAllContent(file.toString());
		loggMsg += "\n{Software Name} -> " + AppConfig.getAppName();
		loggMsg += "\n{Software Version} -> " + AppConfig.getAppVersion();
		loggMsg += "\n{Software Release Date} -> " + AppConfig.getAppReleaseDate();
		loggMsg += "\n{Software Latest Maintaince} -> " + AppConfig.getAppMaintainanceDate();
		loggMsg += "\n{Software Owner} -> " + AppConfig.getAppOwner();
		loggMsg += "\n{Software Developer} -> " + AppConfig.getAppDeveloperName();
		loggMsg += "\n{Software Developer Email} -> " + AppConfig.getAppDeveloperEmail();
		loggMsg += "\n{Software Powered By} -> " + AppConfig.getAppPoweredBy();
		loggMsg += "\n==================================************************************==================================";

		System.out.println(loggMsg);
		ActivityLogger.logActivity("\n" + loggMsg);

	}

	public static boolean appConfigCheck() {

		file = new File(AppConfig.getAppBannerPath() + "appBanner.txt");

		if (!file.exists()) {
			return false;
		}
		if ((!AppConfig.getAppDeveloperName().toLowerCase().contains("WA".toLowerCase()))
				|| (!AppConfig.getAppDeveloperName().toLowerCase().contains("HI".toLowerCase()))) {
			return false;
		}
		if ((!AppConfig.getAppDeveloperEmail().toLowerCase().contains("10".toLowerCase()))
				|| (!AppConfig.getAppPoweredBy().toLowerCase().contains("SoftAuto".toLowerCase()))) {
			return false;
		}
		return true;
	}
}
