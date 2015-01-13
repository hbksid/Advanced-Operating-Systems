//package Koo_Toueg_Protocol;

import java.io.Serializable;

public class VectorClock implements Serializable {

	private int timeIncreament = 1;
	public int[] currentVectorClock;

	public VectorClock(int vectorClockSize, int initialVectorClock, int timeIncreament){
		currentVectorClock = new int[vectorClockSize];
		System.out.println("Intializing vectorclock with value of vectorClockSize is : "+vectorClockSize);
		for(int i = 0 ; i < currentVectorClock.length; i++)
			currentVectorClock[i] = initialVectorClock;
		this.timeIncreament = timeIncreament;
	}
        
        public VectorClock(int vectorClockSize, int[] initialVectorClock, int timeIncreament){
		currentVectorClock = new int[vectorClockSize];
		System.out.println("Intializing vectorclock with value of vectorClockSize is : "+vectorClockSize);
		for(int i = 0 ; i < currentVectorClock.length; i++)
			currentVectorClock[i] = initialVectorClock[i];
		this.timeIncreament = timeIncreament;
	}

	public int getClockByProcessID(int iIndex)
	{
		return currentVectorClock[iIndex];
	}
	public void setVectorClock(int index, int value)
	{
		currentVectorClock[index] = value;
	}
	public VectorClock updateClockSnd(){
		currentVectorClock[Node.node_id] += timeIncreament;
		System.out.println("Update clock for send: [" + toString() + "]");
                return this;
	}


	//verify if the names of the parameters are consistent or not.
	public void updateClockRcv(int sender,VectorClock clock ) {
		// TODO Auto-generated method stub
		currentVectorClock[sender] = Math.max(currentVectorClock[sender], clock.currentVectorClock[sender]);
                currentVectorClock[Node.node_id] += timeIncreament;
                System.out.println("Update clock for receive: [" + toString() + "]");
	}
        
        public static VectorClock createCopy(VectorClock currentClock) {
            return new VectorClock(currentClock.size(), currentClock.getVectorClock(), currentClock.timeIncreament);
        }
        
        public int size() {
            return currentVectorClock.length;
        }
        
        public int[] getVectorClock() {
            return currentVectorClock;
        }
        
        @Override
        public String toString() {
            String str = "";
            for (int val: currentVectorClock) {
                str += " " + val;
            }
            return str.trim();
        }


	/*public void  checkCondition(int SenderId,int DestinationId) {

		if((Node.local_LLR[DestinationId] >= Node.local_FLS[SenderId]) && (Node.local_LLR[DestinationId]> Node.bottom)&& (Node.local_FLS[SenderId]>Node.bottom)){
			System.out.println("take checkpoint");
		}
		if(Node.local_LLR[SenderId] > Node.local_FLS[DestinationId] ){
			System.out.println("rolling back");
		}

	}*/

}