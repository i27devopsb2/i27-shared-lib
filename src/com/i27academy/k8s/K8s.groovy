package com.i27academy.k8s

class K8s {
    def jenkins
    K8s(jenkins) {
        this.jenkins = jenkins
    }

    def auth_login(gke_cluster_name, gke_zone, gke_project) {
        jenkins.sh """#!/bin/bash
        echo "Entering Authentication method for GKE Cluster Login"
        gcloud config set account jenkins@delta-sprite-416312.iam.gserviceaccount.com
        # gcloud auth activate-service-account jenkins@nice-carving-411801.iam.gserviceaccount.com --key-file=key.json
        gcloud compute instances list
        echo "************* Listing Number of Nodes in K8S *************"
        gcloud container clusters get-credentials $gke_cluster_name --zone $gke_zone --project $gke_project
        kubectl get nodes
        """
    }
    def k8sdeploy(fileName, docker_image , namespace){
        jenkins.sh """#!/bin/bash
        echo "Executing K8S Deploy Method"
        echo "Final Image Tag is $docker_image"
        sed -i "s|DIT|$docker_image|g" ./.cicd/$fileName
        kubectl apply -f ./.cicd/$fileName -n $namespace
        """
    }

    def k8sHelmChartDeploy(appName, env, helmChartPath, imageTag) {
       jenkins.sh """#!/bin/bash
       echo "*************** Helm Groovy method Starts here ***************"
       echo "Checking if helm chart exists"
       if helm list | grep -q "${appName}-${env}-chart"; then
        echo "Chart Exists !!!!!!!!!"
        echo "Upgrading the Chart !!!!!!"
        helm upgrade ${appName}-${env}-chart -f ./.cicd/k8s/values_${env}.yaml --set image.tag=${imageTag} ${helmChartPath}
       else 
        echo "Installing the Chart"
        helm install ${appName}-${env}-chart -f ./.cicd/k8s/values_${env}.yaml --set image.tag=${imageTag} ${helmChartPath}
       fi
       # helm install chartname -f valuesfilepath chartpath
       # helm upgrade chartname -f valuefilepath chartpath
       """ 
    }

    def gitClone() {
       jenkins.sh """#!/bin/bash
       echo "*************** Entering Git Clone Method ***************"
       git clone -b master https://github.com/i27devopsb2/i27-shared-lib.git
       echo "Listing the files"
       ls -la 
       echo "Showing the files under i27-shared-lib repo"
       ls -la i27-shared-lib
       echo "Showing the files under chart folder"
       ls -la i27-shared-lib/chart/
       echo "Showing the link in default folder"
       ls -la i27-shared-lib/src/com/i27academy/k8s/default/
       """ 
    }

    def namespace_creation(namespace_name) {
        jenkins.sh """#!/bin/bash
        # This Groovy method is to create a namespace in the respective k8s cluster
        #!/bin/bash
        # write a pipeline or a groovy method, to make sure i create the namesapces from the jenkins.
        
        #namespace_name="boutique"
        echo "Namespace given is ${namespace_name}"
         
        # Validate If the namespace is Empty

        if [ -z "${namespace_name}" ]; then
          echo "Error: Namespace Cant be Empty"
          exit 1
        fi
        # Lets Verify if Kubernetes namespace exists 
        if kubectl get namespace "${namespace_name}" &> /dev/null ; then 
        echo "Your kubernetes namesapce '${namespace_name}' exists"
        exit 0
        else 
        echo "Your Namespace '${namespace_name}' doesnot exists, so creating it !!!!!!"
        if kubectl create namespace "${namespace_name}" &> /dev/null; then 
            echo "Your Namespace '${namespace_name}' has created succesfully"
            exit 0
        else
            echo "Some Error, failed to create namespace '${namespace_name}'"
            exit 1
        fi 
        fi

        """

    }
    
    def netpolReplace (filename, namespace, replace_netpol_name) {
        jenkins.sh """#!/bin/bash
        echo "This is from Netpol groovy method"
        fname=${filename}
        echo \${fname}
        sed -i 's/network-allow/${replace_netpol_name}/' \${fname}
        kubectl apply -f \${fname} -n ${namespace}
        echo "Network policy created succesfully"
        """
    }
}