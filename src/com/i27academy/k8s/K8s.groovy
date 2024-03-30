package com.i27academy.k8s

class K8s {
    def jenkins
    K8s(jenkins) {
        this.jenkins = jenkins
    }

    def auth_login(gke_cluster_name, gke_zone, gke_project) {
        jenkins.sh """#!/bin/bash
        echo "Entering Authentication method for GKE Cluster Login"
        gcloud config set account jenkins@nice-carving-411801.iam.gserviceaccount.com
        # gcloud auth activate-service-account enkins@nice-carving-411801.iam.gserviceaccount.com --key-file=key.json
        gcloud compute instances list
        echo "************* Listing Number of Nodes in K8S *************"
        gcloud container clusters get-credentials $gke_cluster_name --zone $gke_zone --project $gke_project
        kubectl get nodes
        """
    }
    def k8sdeploy(){
        jenkins.sh """#!/bin/bash
        echo "Executing K8S Deploy Method"
        kubectl apply -f ./.cicd/k8s_dev.yaml
        """
    }
    
}