/* 
* Copyright (C) 2007, Brian Tanner
* 
http://rl-glue.googlecode.com/

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.rlcommunity.rlglue.tests;


//import rlVizLib.utilities.TaskSpecObject;
import org.rlcommunity.rlglue.utilities.TaskSpec;
import org.rlcommunity.rlglue.agent.AgentInterface;
import org.rlcommunity.rlglue.types.Action;
import org.rlcommunity.rlglue.types.Observation;

public class Test_1_Agent implements AgentInterface {
	int stepCount=0;
	
    public RandomAgent(){
        }

	public void agent_init(String taskSpecString) {

	}
	
	public Action agent_start(Observation o) {
		stepCount=0;
		return new Action(o);
	}
	
	public Action agent_step(double arg0, Observation o) {
		stepCount++;
		return new Action(o);
	}
	
	public void agent_end(double arg0) {
            // TODO Auto-generated method stub
		
	}
	
	public String agent_message(String arg0) {
		int timesToPrint=stepCount%3;
		StringBuffer b=new StringBuffer();
		
            // TODO Auto-generated method stub
            return null;
	}
	

	public void agent_cleanup() {
            // TODO Auto-generated method stub
		
	}





}
