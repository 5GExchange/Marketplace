#!groovy
timestamps {
    node {
        catchError {
            checkout scm
            def marketplaceVersion = "1.0.0.${env.BUILD_NUMBER}"
            // Make sure the TomcatEE binary is there
            sh '''
            if [ ! -f NFS/server/apache-tomee-1.7.4-plus.zip ]; then
                mkdir -p NFS/server
                rm -f NFS/server/*
                wget -q -P NFS/server https://www.apache.org/dist/tomee/tomee-1.7.4/apache-tomee-1.7.4-plus.zip
            fi
            '''
            docker.image('frekele/ant:1.9.7-jdk8').withRun('-v /var/lib/ivy2/cache:/root/.ivy2/cache') {c ->
                sh """
                docker cp NFS ${c.id}:/root
                docker exec ${c.id} ant -f NFS/build.xml -lib NFS/lib.no.deploy
                rm -rf NFS/dist
                docker cp ${c.id}:/root/NFS/dist NFS
                """
            }
            buildImage("nfs:${marketplaceVersion}", "NFS")
            buildImage("marketplace-mysql:${marketplaceVersion}", "marketplace/mysql")
            buildImage("marketplace-dashboard:${marketplaceVersion}", "marketplace/dashboard")
            buildImage("marketplace-umaa:${marketplaceVersion}", "marketplace/umaa")
            buildImage("marketplace-vnfs:${marketplaceVersion}", "marketplace/vnfs")
            buildImage("marketplace-broker:${marketplaceVersion}", "marketplace/broker")
            buildImage("marketplace-cli:${marketplaceVersion}", "marketplace/marketplace-cli")
            buildImage("marketplace-mdc:${marketplaceVersion}", "marketplace/mdc")
            // Run bash with -it to keep the container alive while we copy files in and run the build
            docker.image('maven:3.3.3-jdk-8').withRun('-it -v /var/lib/m2:/root/.m2', 'bash') {c ->
                sh """
                docker cp marketplace/service-catalog ${c.id}:/root
                docker exec ${c.id} mvn -f /root/service-catalog package
                mkdir -p marketplace/service-catalog/target
                docker cp ${c.id}:/root/service-catalog/target/service-catalog-1.0.jar marketplace/service-catalog/target/service-catalog-1.0.jar
                """
            }
            buildImage("marketplace-service-catalog:${marketplaceVersion}", "marketplace/service-catalog")
            docker.image('maven:3.3.3-jdk-8').withRun('-it -v /var/lib/m2:/root/.m2', 'bash') {c ->
                sh """
                docker cp marketplace/service-selection ${c.id}:/root
                docker exec ${c.id} mvn -f /root/service-selection package
                mkdir -p marketplace/service-selection/target
                docker cp ${c.id}:/root/service-selection/target/service-selection-1.0.jar marketplace/service-selection/target/service-selection-1.0.jar
                """
            }
            buildImage("marketplace-service-selection:${marketplaceVersion}", "marketplace/service-selection")
            buildImage("marketplace-accounting:${marketplaceVersion}", "marketplace/accounting")
            docker.image('maven:3.3.3-jdk-8').withRun('-it -v /var/lib/m2:/root/.m2', 'bash') {c ->
                sh """
                docker cp marketplace/sla-core ${c.id}:/root
                docker exec ${c.id} mvn -f /root/sla-core verify
                mkdir -p marketplace/sla-core/sla-service/target/dependency
                docker cp ${c.id}:/root/sla-core/sla-service/target/dependency/jetty-runner.jar marketplace/sla-core/sla-service/target/dependency/jetty-runner.jar
                docker cp ${c.id}:/root/sla-core/sla-service/target/sla-service.war marketplace/sla-core/sla-service/target/sla-service.war
                """
            }
            buildImage("marketplace-sla:${marketplaceVersion}", "marketplace/sla-core")
            buildImage("marketplace-proxy:${marketplaceVersion}", "marketplace/proxy")
        }
        step([$class: 'Mailer', recipients: '5gex-devel@tmit.bme.hu'])
    }
}

def buildImage(String tag, String args = '.') {
    docker.withRegistry('https://5gex.tmit.bme.hu') {
        def image = docker.build(tag, args)
        image.push()
        image.push('latest')
    }
}
