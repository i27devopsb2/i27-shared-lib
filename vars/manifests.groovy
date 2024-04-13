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
            string (name: 'ADD_PVC' , description: "Enter the PVC size u want to have in your namespace")
            string (name: 'netpolName', description: "Enter the Name for the netpol")
        }
        environment {
            APPLICATION_NAME = "${pipelineParams.appName}"
            //APPLICATION_NAME = "eureka"
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
            NETPOL_PATH = "./i27-shared-lib/src/com/i27academy/k8s/default/netpol-generic.yaml"
            NETPOL_NAME = "${params.netpolName}"

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
            stage ('Manifest Opration') {
                steps {
                    script {
                        println("Starting The Manifest Operation stage")
                        k8s.netpolReplace(env.NETPOL_PATH, "${params.NAMESPACE_NAME}","${params.NAMESPACE_NAME}"+'-'+env.NETPOL_NAME)
                        // mysql network policy
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




