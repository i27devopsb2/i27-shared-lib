import com.i27academy.builds.Docker
import com.i27academy.k8s.K8s

library ('com.i27academy.slb')

def call(Map pipelineParams) {
    Docker docker = new Docker(this)
    K8s k8s = new K8s(this)
    pipeline {
        agent any
        parameters {
            string (name: 'NAMESPACE_NAME', description: 'Enter the name of the k8s namespace to be created')
        }
        environment {
            APPLICATION_NAME = "${pipelineParams.appName}"
            //APPLICATION_NAME = "eureka"
            POM_VERSION = readMavenPom().getVersion()
            POM_PACKAGING = readMavenPom().getPackaging()
            //version+ packaging
            DOCKER_HUB = "docker.io/i27devopsb2"
            DOCKER_CREDS = credentials('i27devopsb2_docker_creds')
            SONAR_URL = "http://34.122.97.102:9000"
            SONAR_TOKEN = credentials('sonar_creds')
            GKE_DEV_CLUSTER_NAME = "operation-cluster"
            GKE_DEV_ZONE = "us-central1-c"
            GKE_DEV_PROJECT = "delta-sprite-416312"
            GKE_TST_CLUSTER_NAME = "tst-cluster"
            GKE_TST_ZONE = "us-west1-b"
            GKE_TST_PROJECT = "nice-carving-4118012"   
            DOCKER_IMAGE_TAG = sh(script: 'git log -1 --pretty=%h', returnStdout:true).trim()
            K8S_DEV_FILE = "k8s_dev.yaml"
            K8S_TST_FILE = "k8s_tst.yaml"
            K8S_STAGE_FILE = "k8s_stg.yaml"
            K8S_PROD_FILE = "k8s_prd.yaml"
            DEV_NAMESPACE = "cart-dev-ns"
            TEST_NAMESPACE = "cart-tst-ns"
            DEV_ENV = "dev"
            TST_ENV = "tst"
            HELM_PATH = "${WORKSPACE}/i27-shared-lib/chart"
            JFROG_DOCKER_REGISTRY = "flipcart.jfrog.io"
            JFROG_DOCKER_REPO_NAME = "images-docker"
            JFROG_CREDS = credentials('JFROG_CREDS')
        }
        stages {
            stage ('Checkout') {
                steps {
                    println("Checkout: Git clone for i27SharedLibr Starting.........")
                    script {
                        k8s.gitClone()
                    }
                }
            }
            stage ('Authenticate to Google Cloud GKE') {
                steps {
                    echo "Executing in Google Cloud auth stage"
                    script {
                        //gke_cluster_name, gke_zone, gke_project 
                        k8s.auth_login("${env.GKE_DEV_CLUSTER_NAME}", "${env.GKE_DEV_ZONE}", "${env.GKE_DEV_PROJECT}")
                    }  
                }
            }
            stage ('Create K8S Namespace') {
                steps {
                    script {
                        k8s.namespace_creation("${params.NAMESPACE_NAME}")
                    }
                }
            }
            stage ('clean'){
                steps {
                    cleanWs()
                }
            }
        }
    }
}




