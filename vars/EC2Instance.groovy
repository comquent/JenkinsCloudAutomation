import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials

import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.DescribeInstancesResult

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.CredentialsProvider

def setCredential(value) {
	credential = value
}

def listInstances() {
    withCredentials([
        usernamePassword(credentialsId: credential, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')
    ]) {

		def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
		def client= AmazonEC2ClientBuilder.standard().withCredentials(credentials).build()
		DescribeInstancesRequest request = new DescribeInstancesRequest()
	
		DescribeInstancesResult result = client.describeInstances(request)
		result.reservations.each{
			it.instances.each{
				echo it.instanceId
			}
		}
	}
}

def launchEC2Instance() {
	return 'DRAFT'
}

def getIpAdress(id) {
	return 'DRAFT'
}

def terminate(id) {
}
