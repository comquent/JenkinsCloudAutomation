import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials

import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.DescribeInstancesResult
import com.amazonaws.services.ec2.model.RunInstancesRequest
import com.amazonaws.services.ec2.model.RunInstancesResult

import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateTagsResult;

import com.amazonaws.services.ec2.model.TerminateInstancesRequest
import com.amazonaws.services.ec2.model.TerminateInstancesResult

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

// TODO: return ids

def listInstances() {
	DescribeInstancesRequest request = new DescribeInstancesRequest()
	DescribeInstancesResult result = getClient().describeInstances(request)
	result.reservations.each{
		it.instances.each{
			echo it.instanceId
		}
	}
}

def createEC2Instances(count) {
	def instanceIds = []
	RunInstancesRequest request = new RunInstancesRequest()
	request.withImageId(imageId).withInstanceType(instanceType)
		.withMinCount(1).withMaxCount(count)
		.withKeyName(keyName).withSecurityGroups([secGroup])

	getClient().runInstances(request).reservation.instances.each{
		instanceIds << it.instanceId
	}	
	return instanceIds
}

def createByType(count, type_name) {
	def instanceIds = []
	RunInstancesRequest run_request = new RunInstancesRequest()
	run_request.withImageId(imageId).withInstanceType(instanceType)
		.withMinCount(1).withMaxCount(count)
		.withKeyName(keyName).withSecurityGroups([secGroup])

	getClient().runInstances(run_request).reservation.instances.each{
		instanceIds << it.instanceId
	}
	
	// add the tag
	Tag tag = new Tag().withKey("Name").withValue(type_name)
	CreateTagsRequest tag_request = new CreateTagsRequest().withTags(tag).withResources(instanceIds)
	CreateTagsResult tag_response = getClient().createTags(tag_request)
	
	return instanceIds
}

def createSingleEC2Instance() {
	ids = createEC2Instances(1)
	return ids.first()
}

def getPublicDnsNames(instanceIds) {
    def publicDnsNames = []
	DescribeInstancesRequest request = new DescribeInstancesRequest()
	request.setInstanceIds(instanceIds)
	DescribeInstancesResult result = getClient().describeInstances(request)
	result.reservations.each{
		it.instances.each{
			publicDnsNames << it.publicDnsName
		}
	}
	return publicDnsNames
}

def getPublicDnsName(instanceId) {
	return getPublicDnsNames([instanceId]).first()
}

def terminateInstances(instanceIds) {
    TerminateInstancesRequest request = new TerminateInstancesRequest(instanceIds)
    TerminateInstancesResult result = getClient().terminateInstances(request)
	def terminateIds = []
	result.terminatingInstances.each{
		terminateIds << it.instanceId
	}
	println "Terminating instance has been triggered, ids = '${terminateIds}'"
}

def terminateInstance(instanceId) {
	terminateInstances([instanceId])
}

def waitForState(instanceIds, state) {
    timeout(3) {
        waitUntil {
			def states = []
			DescribeInstancesRequest request = new DescribeInstancesRequest()
			request.setInstanceIds(instanceIds)
			DescribeInstancesResult result = getClient().describeInstances(request)
			result.reservations.each{
				it.instances.each{
//					echo "... State: ${it.state.name} (${it.state.code})"
					states << it.state
				}
			}
			println "current states: ${states}"
			sum_state = state
			states.each{
				if (it.code != state) {
					sum_state = it.code
				}
			}
			if(sum_state == state) {
				return true
			}
			sleep(time: 5)
			return false
		}
	}

}

def waitForRunning(ids) {
	waitForState(ids, 16)
}

def call(count = 1, body) {
	def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = config
	
	echo "*** start creation instances ***"
	ids = createEC2Instances(count)
	println "created ids: ${ids}"
	waitForRunning(ids)
	echo "*** creation instances done ***"
	body.dnsNames = getPublicDnsNames(ids)

	body()
	echo "*** start termination instances ***"
	terminateInstances(ids)
	echo "*** termination has been triggered ***"
}
