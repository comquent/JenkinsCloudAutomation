import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.ec2.model.RunInstancesRequest
import com.amazonaws.services.ec2.model.RunInstancesResult
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials

import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.DescribeInstancesResult

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.CredentialsProvider

def setCredential(value) {
	credential = value
}

def getClient() {
    withCredentials([
        usernamePassword(credentialsId: credential, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')
    ]) {

		def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
		return AmazonEC2ClientBuilder.standard().withCredentials(credentials).build()
	}
}

def listInstances() {
	DescribeInstancesRequest request = new DescribeInstancesRequest()
	DescribeInstancesResult result = getClient().describeInstances(request)
	result.reservations.each{
		it.instances.each{
			echo it.instanceId
		}
	}
}

def launchEC2Instance(imageId, instanceType = "t2.nano", keyName, secGroup) {
	echo "Launch Instance, imageId = " + imageId
	RunInstancesRequest request = new RunInstancesRequest()
	request.withImageId(imageId).withInstanceType(instanceType)
		.withMinCount(1).withMaxCount(1)
		.withKeyName(keyName).withSecurityGroups([secGroup])

	RunInstancesResult result = getClient().runInstances(request)
	return result.reservation.instances.first().instanceId
}

def getIpAdress(id) {
	return "DRAFT"
}

def terminate(id) {
}
