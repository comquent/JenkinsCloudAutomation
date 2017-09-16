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

def withCredential(value) {
	credential = value
	return this
}

def setImageId(value) {
	imageId = value
}

def withImageId(value) {
	imageId = value
	return this
}

def setKeyName(value) {
	keyName = value
}

def withKeyName(value) {
	keyName = value
	return this
}

def setSecGroup(value) {
	secGroup = value
}

def withSecGroup(value) {
	secGroup = value
	return this
}

def setInstanceType(value) {
	instanceType = value
}

def withInstanceType(value) {
	instanceType = value
	return this
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

def launchEC2Instance() {
	RunInstancesRequest request = new RunInstancesRequest()
	request.withImageId(imageId).withInstanceType(instanceType)
		.withMinCount(1).withMaxCount(1)
		.withKeyName(keyName).withSecurityGroups([secGroup])

	RunInstancesResult result = getClient().runInstances(request)
	return result.reservation.instances.first().instanceId
}

def getPublicDnsName(id) {	
	DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest()
    request.setInstanceIds([instanceId])
	DescribeInstancesResult result = getClient().describeInstances(request)
	return result.reservations.first().instances.first().publicDnsName
}

def terminate(id) {
}
