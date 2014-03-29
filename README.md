

	In case you need to re-install the Saba's jar files:

	#Sabasecurity.jar
	mvn install:install-file -DgroupId=com.saba -DartifactId=sabasecurity -Dversion=5.5.1.1_0070980_0081450_0075361_0076700_0078533_0074265   -Dpackaging=jar -DlocalRepositoryPath=local-maven-repo -Dfile=saba/5.5.1.1_0070980_0081450_0075361_0076700_0078533_0074265/sabasecurity.jar
	#Sabares.jar
	mvn install:install-file -DgroupId=com.saba -DartifactId=sabares -Dversion=5.5.1.1_0070980_0081450_0075361_0076700_0078533_0074265   -Dpackaging=jar -DlocalRepositoryPath=local-maven-repo -Dfile=saba/5.5.1.1_0070980_0081450_0075361_0076700_0078533_0074265/sabares.jar
	#Saba.jar
	mvn install:install-file -DgroupId=com.saba -DartifactId=saba -Dversion=5.5.1.1_0070980_0081450_0075361_0076700_0078533_0074265   -Dpackaging=jar -DlocalRepositoryPath=local-maven-repo -Dfile=saba/5.5.1.1_0070980_0081450_0075361_0076700_0078533_0074265/saba.ear/saba.jar
	