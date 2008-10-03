# 
# Copyright (C) 2008, Brian Tanner
# 
#http://rl-glue-ext.googlecode.com/
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import random
import sys
from rlglue.environment.Environment import Environment
from rlglue.environment import EnvironmentLoader as EnvironmentLoader
from rlglue.types import Observation
from rlglue.types import Action
from rlglue.types import Reward_observation
from rlglue.types import State_key
from rlglue.types import Random_seed_key

class test_1_environment(Environment):
	stepCount=0
	o=Observation()
	
	def env_init(self):  
		return "sample task spec"
	
	def env_start(self):
		self.stepCount=0
		
		self.o.intArray=[1]
		self.o.doubleArray=[0.0/2.0, 1.0/2.0]
		self.o.charArray=['a','b','c']

		return self.o;
	
	def env_step(self,action):
		self.o.doubleArray=[]
		self.o.charArray=[]
		self.o.intArray=[self.stepCount]
		
		self.stepCount=self.stepCount+1
		
		terminal=False
		
		if self.stepCount==5:
			terminal=True
		
		ro=Reward_observation()
		ro.r=1.0
		ro.o=self.o
		ro.terminal=terminal
		
		return ro	

	def env_cleanup(self):
		pass
	
	def env_set_state(self, stateKey):
		pass
	
	def env_set_random_seed(self, randomSeedKey):
		pass
	
	def env_get_state(self):
		return State_key()
	
	def env_get_random_seed(self):
		return Random_seed_key()
	
	def env_message(self,inMessage):
		timesToPrint=self.stepCount%3
		
		outMessage=inMessage+"|"
		for i in range(0, timesToPrint):
			outMessage=outMessage+"%d" % (self.stepCount)
			outMessage=outMessage+"."

		outMessage=outMessage+"|"+inMessage
		
		return outMessage
	
if __name__=="__main__":
	EnvironmentLoader.loadEnvironment(test_1_environment())