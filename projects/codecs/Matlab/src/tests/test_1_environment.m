function theEnvironment=test_1_environment()
    theEnvironment.env_init=@test_1_environment_init;
    theEnvironment.env_start=@test_1_environment_start;
    theEnvironment.env_step=@test_1_environment_step;
    theEnvironment.env_load_state=@test_1_environment_load_state;
    theEnvironment.env_save_state=@test_1_environment_save_state;
    theEnvironment.env_load_random_seed=@test_1_environment_load_random_seed;
    theEnvironment.env_save_random_seed=@test_1_environment_save_random_seed;
    theEnvironment.env_cleanup=@test_1_environment_cleanup;
    theEnvironment.env_message=@test_1_environment_message;
end

function taskSpec=test_1_environment_init()
	taskSpec='sample task spec';
end    


function theObservation=test_1_environment_start()
    global test_1_environment_struct;
    test_1_environment_struct.stepCount=0;
    theObservation = org.rlcommunity.rlglue.codec.types.Observation();
	theObservation.intArray=[0];
	theObservation.doubleArray=[0/2 1/2];
	theObservation.charArray=['abc'];
end

function rewardObservation=test_1_environment_step(theAction)
    global test_1_environment_struct;

	theObservation = org.rlcommunity.rlglue.codec.types.Observation();
	theObservation.intArray=[test_1_environment_struct.stepCount];

    test_1_environment_struct.stepCount=test_1_environment_struct.stepCount+1;

	terminal=0;
	
	if test_1_environment_struct.stepCount==5
		terminal=1;
    end
    
	rewardObservation=org.rlcommunity.rlglue.codec.types.Reward_observation_terminal(1.0,theObservation,terminal);
	
end

function returnMessage=test_1_environment_message(theMessageJavaObject)
	global test_1_environment_struct;
    theMessage=char(theMessageJavaObject);
    
    timesToPrint=mod(test_1_environment_struct.stepCount,3);
    
    returnMessage=sprintf('%s|',theMessage);
    %Start at one because the top end will be inclusive unlike C for loop
    for i=1:1:timesToPrint
        returnMessage=sprintf('%s%d.',returnMessage,test_1_environment_struct.stepCount);
    end
    returnMessage=sprintf('%s|%s',returnMessage,theMessage);
end


function test_1_environment_cleanup()
end

function test_1_environment_load_seed(theState)
end

function test_1_environment_load_random_seed(randomSeed)
end

function theState=test_1_environment_save_state()
end

function randomSeed=test_1_environment_save_random_seed()
end
