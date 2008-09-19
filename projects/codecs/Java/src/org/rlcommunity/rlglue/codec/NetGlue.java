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
* 
*  $Revision: 151 $
*  $Date: 2008-09-17 20:09:24 -0600 (Wed, 17 Sep 2008) $
*  $Author: brian@tannerpages.com $
*  $HeadURL: https://rl-glue-ext.googlecode.com/svn/trunk/projects/codecs/Java/src/org/rlcommunity/rlglue/codec/RLGlue.java $
* 
*/


package org.rlcommunity.rlglue.codec;


import java.io.IOException;
import org.rlcommunity.rlglue.codec.network.Network;
import org.rlcommunity.rlglue.codec.types.Observation_action;
import org.rlcommunity.rlglue.codec.types.Random_seed_key;
import org.rlcommunity.rlglue.codec.types.Reward_observation_action_terminal;
import org.rlcommunity.rlglue.codec.types.State_key;

public class NetGlue implements RLGlueInterface
{

	private     Network network;

        private  void forceConnection()
	{
		if (network == null)
		{   
			network = new Network();

			// Connect
			network.connect(Network.kDefaultHost, 
					Network.kDefaultPort, 
					Network.kRetryTimeout);

			network.clearSendBuffer();
			network.putInt(Network.kExperimentConnection);
			network.putInt(0);
			network.flipSendBuffer();

			try 
			{
				network.send();
			}
			catch(IOException ioException)
			{
				ioException.printStackTrace();
				System.exit(1);
			}
		}
	}

	private  synchronized void doStandardRecv(int state) throws IOException
	{
		network.clearRecvBuffer();
		
		int recvSize = network.recv(8) - 8;

		int glueState = network.getInt(0);
		int dataSize = network.getInt(Network.kIntSize);
		int remaining = dataSize - recvSize;

		if (remaining < 0)
			remaining = 0;
		
		int remainingReceived=network.recv(remaining);

		network.flipRecvBuffer();	
		
		// Discard the header - we should have a more elegant method for doing this.
		network.getInt();
		network.getInt();
		
		if (glueState != state)
		{
			System.err.println("Not synched with server. glueState = " + glueState + " but should be " + state);
			System.exit(1);
		}
	}
	
	private  synchronized void doCallWithNoParams(int state) throws IOException
	{		
		network.clearSendBuffer();
		network.putInt(state);
		network.putInt(0);
		network.flipSendBuffer();
		network.send();
	}
	
	public  synchronized String RL_init()
	{
                String task_spec="";
		forceConnection();

		try
		{
			doCallWithNoParams(Network.kRLInit);
			doStandardRecv(Network.kRLInit);
                        task_spec=network.getString();
		}
		catch(IOException ioException)
		{
			ioException.printStackTrace();
			System.exit(1);
		}
                return task_spec;
	}

	public  synchronized Observation_action RL_start()
	{
		Observation_action obsact = null;
		try
		{

			doCallWithNoParams(Network.kRLStart);

			doStandardRecv(Network.kRLStart);
			
			obsact = new Observation_action();

			obsact.o = network.getObservation();

			obsact.a = network.getAction();
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
			System.exit(1);
		}
		catch (NullPointerException nullException)
		{
			System.err.println("You must call RL_init before calling RL_start");
			nullException.printStackTrace();
			System.exit(1);
		}


		return obsact;
	}

	public  synchronized Reward_observation_action_terminal RL_step()
	{
		Reward_observation_action_terminal roat = null;
		try 
		{
			doCallWithNoParams(Network.kRLStep);
			doStandardRecv(Network.kRLStep);
			
			roat = new Reward_observation_action_terminal();
			roat.terminal = network.getInt();
			roat.r = network.getDouble();
			roat.o = network.getObservation();
			roat.a = network.getAction();

			return roat;
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
			System.exit(1);
		}
		catch (NullPointerException nullException)
		{
			System.err.println("You must call RL_init before calling RL_step");
			nullException.printStackTrace();
			System.exit(1);
		}
		return roat;
	}

	public  synchronized void RL_cleanup()
	{
		try
		{
			doCallWithNoParams(Network.kRLCleanup);
			doStandardRecv(Network.kRLCleanup);

			//network.close(); // Cleanup no longer closes the connection.  
			
			//We need to be able to run multiple RL_init/RL_cleanup's without killing the 
			//connection between.  Since this code is running from the user experiment
			//the connection never gets explicitly closed.  :(
			//The VM/OS will clean this up and close the connection when the program exits.
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
			System.exit(1);
		}
		catch (NullPointerException nullException)
		{
			System.err.println("You must call RL_init before calling RL_cleanup");
			nullException.printStackTrace();
			System.exit(1);
		}
	}

	public  synchronized String RL_agent_message(String message)
	{		
		String response = "";
		forceConnection();

		try
		{
			network.clearSendBuffer();
			network.putInt(Network.kRLAgentMessage);
			network.putInt(Network.sizeOf(message));
			network.putString(message);
			network.flipSendBuffer();
			network.send();

			doStandardRecv(Network.kRLAgentMessage);
			response = network.getString();
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
			System.exit(1);
		}
		return response;
	}

	public  synchronized String RL_env_message(String message)
	{
		String response = "";
		forceConnection();
				
		try
		{

			network.clearSendBuffer();
			network.putInt(Network.kRLEnvMessage);
			network.putInt(Network.sizeOf(message));
			network.putString(message);
			network.flipSendBuffer();
			network.send();

			doStandardRecv(Network.kRLEnvMessage);
			response = network.getString();

		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
			System.exit(1);
		}
		return response;
	}

	public  synchronized double RL_return()
	{
		double reward = 0.0;

		try
		{
			doCallWithNoParams(Network.kRLReturn);
			doStandardRecv(Network.kRLReturn);
			reward = network.getDouble();
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
			System.exit(1);
		}
		catch (NullPointerException nullException)
		{
			System.err.println("You must call RL_init before calling RL_return");
			nullException.printStackTrace();
			System.exit(1);
		}
		return reward;
	}

	public  synchronized int RL_num_steps()
	{
		int numSteps = 0;
		
		try
		{
			doCallWithNoParams(Network.kRLNumSteps);
			doStandardRecv(Network.kRLNumSteps);
			
			numSteps = network.getInt();
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
			System.exit(1);
		}
		catch (NullPointerException nullException)
		{
			System.err.println("You must call RL_init before calling RL_num_steps");
			nullException.printStackTrace();
			System.exit(1);
		}
		
		return numSteps;
	}

	public  synchronized int RL_num_episodes()
	{
		int numEpisodes = 0;
		
		try
		{
			doCallWithNoParams(Network.kRLNumEpisodes);
			doStandardRecv(Network.kRLNumEpisodes);
			numEpisodes = network.getInt();
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
			System.exit(1);
		}
		catch (NullPointerException nullException)
		{
			System.err.println("You must call RL_init before calling RL_num_episodes");
			nullException.printStackTrace();
			System.exit(1);
		}
		return numEpisodes;
	}

	public  synchronized int RL_episode(int numSteps)
	{
                int exitStatus=0;
		try
		{
			network.clearSendBuffer();
			network.putInt(Network.kRLEpisode);
			network.putInt(Network.kIntSize);
			network.putInt(numSteps);
			network.flipSendBuffer();
			network.send();

			doStandardRecv(Network.kRLEpisode);
                        exitStatus=network.getInt();
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
			System.exit(1);
		}
		catch (NullPointerException nullException)
		{
			System.err.println("You must call RL_init before calling RL_episode");
			nullException.printStackTrace();
			System.exit(1);
		}
                return exitStatus;
	}

	public  synchronized void RL_set_state(State_key sk)
	{
		try
		{
			network.clearSendBuffer();
			network.putInt(Network.kRLSetState);
			network.putInt(Network.sizeOf(sk));
			network.putStateKey(sk);
			network.flipSendBuffer();
			network.send();

			doStandardRecv(Network.kRLSetState);
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
			System.exit(1);
		}
		catch (NullPointerException nullException)
		{
			System.err.println("You must call RL_init before calling RL_set_state");
			nullException.printStackTrace();
			System.exit(1);
		}
	}

	public  synchronized void RL_set_random_seed(Random_seed_key rsk)
	{
		try
		{
			network.clearSendBuffer();
			network.putInt(Network.kRLSetRandomSeed);
			network.putInt(Network.sizeOf(rsk));
			network.putRandomSeedKey(rsk);
			network.flipSendBuffer();
			network.send();

			doStandardRecv(Network.kRLSetRandomSeed);
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
			System.exit(1);
		}
		catch (NullPointerException nullException)
		{
			System.err.println("You must call RL_init before calling RL_set_random_seed");
			nullException.printStackTrace();
			System.exit(1);
		}
	}

	public  synchronized State_key RL_get_state()
	{
		State_key key = null;
		
		try
		{
			doCallWithNoParams(Network.kRLGetState);
			doStandardRecv(Network.kRLGetState);
			
			key = network.getStateKey();
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
			System.exit(1);
		}
		catch (NullPointerException nullException)
		{
			System.err.println("You must call RL_init before calling RL_get_state_key");
			nullException.printStackTrace();
			System.exit(1);
		}
		return key;
	}

	public  synchronized Random_seed_key RL_get_random_seed()
	{
		Random_seed_key key = null;
		
		try
		{
			doCallWithNoParams(Network.kRLGetRandomSeed);
			doStandardRecv(Network.kRLGetRandomSeed);
			
			key = network.getRandomSeedKey();
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
			System.exit(1);
		}
		catch (NullPointerException nullException)
		{
			System.err.println("You must call RL_init before calling RL_get_state_key");
			nullException.printStackTrace();
			System.exit(1);
		}
		
		return key;
	}

}
