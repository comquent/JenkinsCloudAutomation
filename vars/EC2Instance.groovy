import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials

import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.DescribeInstancesResult
import com.amazonaws.services.ec2.model.RunInstancesRequest
import com.amazonaws.services.ec2.model.RunInstancesResult
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

def createSingleEC2Instance() {
	ids = createEC2Instances(1)
	return ids.first()
}

def getPublicDnsNames(instanceIds) {
    def publicDnsNames = []
    timeout(5) {
        waitUntil {
			def states = []
			DescribeInstancesRequest request = new DescribeInstancesRequest()
			request.setInstanceIds(instanceIds)
			DescribeInstancesResult result = getClient().describeInstances(request)
			result.reservations.each{
				it.instances.each{
					echo "... State: ${it.state.name} (${it.state.code})"
					states << it.state
				}
			}
			sum_state = 16
			states.each{
				if (it.code == 0) {
					sum_state = 0
				}
			}
			if(sum_state == 16) {
				result.reservations.each{
					it.instances.each{
						publicDnsNames << it.publicDnsName
					}
				}
				return true
			}
			sleep(time: 5)
			return false
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
    result.terminatingInstances.each{
		echo "Terminating instance ID ${it.instanceId} has been triggered"
	}
}

def terminateInstance(instanceId) {
	terminateInstances([instanceId])
}

def waitInstances(instanceIds, state) {
    timeout(5) {
        waitUntil {
			def states = []
			DescribeInstancesRequest request = new DescribeInstancesRequest()
			request.setInstanceIds(instanceIds)
			DescribeInstancesResult result = getClient().describeInstances(request)
			result.reservations.each{
				it.instances.each{
					echo "... State: ${it.state.name} (${it.state.code})"
					states << it.state
				}
			}
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
