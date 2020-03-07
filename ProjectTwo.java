// Kaushik Nadimpalli
// CS4348.001
// Project Two - Exploring Multiple Threads

// Project Description: A hotel is simulated by using threads to model customer and employee behavior.  

/* Project Overview: 
The hotel to be simulated has two employees at the front desk to register guests and two bellhops to handle guests’ bags.  
A guest will first visit the front desk to get a room number.  The front desk employee will find an available room and assign it to the guest.  
If the guest has less than 3 bags, the guest proceeds directly to the room.  Otherwise, the guest visits the bellhop to drop off the bags.  
The guest will later meet the bellhop in the room to get the bags, at which time a tip is given. */

import java.util.*;
// imported libraries - random, linkedlist, queue among others 
import java.util.concurrent.Semaphore;
// we must use java.util.concurrent semaphore to create and manage our semaphores in the project

/* Our main class. The simulation will be run through this class and methods from the other classes
   will be accessed in this class. We use a Runnable which is basically a type of class(interface) that can be put into 
   a thread, describing what the thread is supposed to do. Without the runnable interface the Thread class, which is responsible
   to execute your stuff in the other thread, would not have the promise to find a run(). */

public class ProjectTwo implements Runnable
{ 	
    public Thread hotelSimulation;
	// this thread will be used to simulate the customer and employee behavior in a hotel setting
	
	// Below is a list of variables we will be using in our hotel simulation program
	public static int employeeCare[];
    public static int bellHopCare[];
    public static int newGuests;
	
	//------------------------------------Queues------------------------------------------//
	
	// We will be using 2 queues in our program
	// We need a queue for guests and their baggage
    public static Queue<Guest> gQueue;
    public static Queue<Guest> bQueue;
	
	//------------------------------------------------------------------------------------//
	
	
	//------------------------------------Our Semaphores------------------------------------------//
    /* Here is a list of semaphores we will use in our program 
	The semaphores help us by checking the value and then, if it using the resource, 
	changes the value to reflect this so that subsequent semaphore users will know to wait. */		
    public static Semaphore availableCustomer;
    public static Semaphore customerService;
   
    public static Semaphore employee;
    public static Semaphore servedEmployee;
	public static Semaphore allocatedRoom;
    
	public static Semaphore bellHop;
    public static Semaphore baggage;
    public static Semaphore baggageService;
    
	// We will be using three mutex semaphores in our program
	// They provide mutual exclusion, so that only one thread can work with entire buffer
	// As long as the buffer is filled by employee or bellboy, the guest needs to wait(and vice versa)
	public static Semaphore mutexOne;
    public static Semaphore mutexTwo;
    public static Semaphore mutexThree;
    
	// Array of semaphores 
	// We will use them to protect several resources with one semaphore
	public static Semaphore serviceDone[];
    public static Semaphore roomOccupied[];
	
	//--------------------------------------------------------------------------------------------//
	
	//-------------------------------------Static Variables------------------------------------------//
    
	// We can only have 2 employees, 2 bellhops, and 25 guests in total
    public static final int TOTALEMPLOYEES = 2;
    public static final int TOTALBELLHOPS = 2;
	public static final int TOTALGUESTS = 25;
	
	//-----------------------------------------------------------------------------------------------//
	
    // In the below constructor, we are instantiating our semaphores
	// We are also instantiating the queues via linked lists, we have imported this library
 
    public ProjectTwo() 
	{	
		//Semaphores initialized with initial number of permits 
		//these semaphore will guarantee first-in,first-out granting of permits under contention
		
        availableCustomer = new Semaphore(0, true);
        customerService = new Semaphore(0, true);
       
	    employee = new Semaphore(TOTALEMPLOYEES, true);
	    servedEmployee = new Semaphore(0, true);
		allocatedRoom = new Semaphore(0, true);
		
		bellHop = new Semaphore(TOTALBELLHOPS, true);
        baggage = new Semaphore(0, true);
        baggageService = new Semaphore(0, true);
        
		mutexOne = new Semaphore(1, true);
        mutexTwo = new Semaphore(1, true);
        mutexThree = new Semaphore(1, true);
		
		// More details regarding purpose of each semaphore is given in the design document
        
		// these semaphores have intial permit set to 25
		// after 25 guests come in and/or 25 rooms are booked as the number of permits
        serviceDone = new Semaphore[TOTALGUESTS];
        roomOccupied = new Semaphore[TOTALGUESTS];
       
	   	// note that since we are utilizing queues, it is in FIFO order
		// hence we set the above fairness setting to true
	    gQueue = new LinkedList<Guest>();
        bQueue = new LinkedList<Guest>(); 

        employeeCare = new int [TOTALGUESTS];
        bellHopCare = new int [TOTALGUESTS];
        newGuests = 0; //intial value since we don't have any guests at beginning of simulation

		// In the loop below, we are reintializing 2 semaphores
		// As long as we have less thn 25 guests, a new room and new service to a guest can be acquired
        for (int x = 0; x < TOTALGUESTS; x++)
		 {
            serviceDone[x] = new Semaphore(0, true);
            roomOccupied[x] = new Semaphore(0, true);		
            employeeCare[x] = 0;
            bellHopCare[x] = 0;
        }
        hotelSimulation = new Thread(); //set the new thread
    }

	// Our main method, we will run the simulation here
    public static void main(String[] args) 
	{
        ProjectTwo hotelSimulation = new ProjectTwo();
        System.out.println("Simulation starts"); //start the simulation

        
        for (int x = 0; x < TOTALEMPLOYEES; x++) 
		{
            new FrontDeskEmployee(x, hotelSimulation); 
			// this will create 2 front desk employees that we need at start of simulation
        }

        for (int x = 0; x < TOTALBELLHOPS; x++) 
		{
            new Bellhop(x, hotelSimulation);
			// this will create 2 bellhops after the employees are created
        }

        for (int x = 0; x < TOTALGUESTS; x++) 
		{
            new Guest(x, hotelSimulation);
			// this will create the 25 guests we need for the simulation
        }

        /* This loop will keep the program running
			until all the guests are in their rooms and joined */
        while (ProjectTwo.newGuests < TOTALGUESTS) 
		{
            System.out.print("");
        }

        //end of simulation
        System.out.println("Simulation ends");
        System.exit(0); //exit gracefully
    }

    public static void addedGuests() 
		{
			++ProjectTwo.newGuests;
			// increment the number of guests in rooms
			// "guests are joined"
		}

		// we will override this method since we need the run method
		// that is in the other classes. ProjectTwo is the superclass
		// but we need the subclasses' method run()
    @Override
    public void run() 
	{
	}
}

// Class - Front Desk Employee
/* Thread requirements for class: Two employees at the front desk (1 thread each). 
   Checks in a guest, finds available room, and gives room number to guest.
   A guest must receive their room key before the front desk employee can register the next guest. */

class FrontDeskEmployee implements Runnable
{
    // Below are a list of variables we will use in this class
    public Thread hotelemployee; //the employee 
	public int eNumber; //the number of the employee, 0 or 1
	
    public static int ROOMNUMBER = 0;
    //Note we have rooms from 1 to 25

    // Below, we are intiailizing the variables for front desk employee instance
    public FrontDeskEmployee(int number, ProjectTwo hotelSimulation) 
	{
        eNumber = number;
        System.out.println("Front desk employee "  + eNumber + " created"); //create front desk employee
        Thread hotelemployee = new Thread(this);
        hotelemployee.start();
		
		/* start method does the following: causes this thread to begin execution, the Java Virtual Machine 
		calls the run method of this thread.The result is that two threads are running concurrently: the current 
			thread (which returns from the call to the start method) and the other thread (which executes its run method). */
    }

    /* Below, we are running the thread whenever we have an instance of employee
		As previously mentioned override ensures this is only true for instances of employee */
    @Override
    public void run() 
	{
        try 
		{
            while(true) 
			{
                // Wait call. Same as acquire in java. We are waiting until a customer is available to come to the front desk
                ProjectTwo.availableCustomer.acquire();

                // This chunk is our critical section code. We need to give a unique room number to the guest
                ProjectTwo.mutexTwo.acquireUninterruptibly(); //must ensure that when we are holding this we cannot be interrupted
                Guest hotelguest = ProjectTwo.gQueue.remove();
				
                ROOMNUMBER++; //increment so guest don't get sent to or given the same room number
                hotelguest.rNumber = ROOMNUMBER;    
                ProjectTwo.mutexTwo.release(); // signal or release the mutex, we no longer need it so it can go to the next thread sequence

               // This code ensure that we record the employee helping the guest and that we are keeping track of the room number of that guest
                ProjectTwo.employeeCare[hotelguest.gNumber] = eNumber;
                ProjectTwo.allocatedRoom.release();
                System.out.println("Front desk employee " + eNumber + " registers guest " + hotelguest.gNumber + " and assigns room " + hotelguest.rNumber);

                ProjectTwo.serviceDone[hotelguest.gNumber].release(); //release the semaphore and increment the array of semaphore. 1 guest joined
                ProjectTwo.servedEmployee.acquire(); // wait call to check the employee service to guest
                ProjectTwo.employee.release(); // release the employee so that he may serve another guest
            }
        }
        catch(Exception e) //catch any exceptions that may be thrown
			{
				e.printStackTrace();
			}
		// Java finally block is always executed whether exception is handled or not. Java finally block follows try or catch block.
        finally {}
    }
}

// Class - Bellhop
/* Thread requirements for class: Two bellhops (1 thread each).
	Gets bags from guest.
	The same bellhop that took the bags delivers the bags to the guest after the guest is in the room.
	Accepts tip from guest. */

class Bellhop implements Runnable
{
	//Variables we will use in this class
    public Thread bellhopboy; //bellhop
    public int bellHopNumber; //number of bellhop

    // Below, we are intiailizing the variables for bell hop instance
    public Bellhop(int number, ProjectTwo hotelSimulation) 
	{
        bellHopNumber = number;
        System.out.println("Bellhop "  + bellHopNumber + " created"); //creates bellhop, 0 and 1
        Thread bellhopboy = new Thread(this);
        bellhopboy.start();
    }

    /* For every given instance of bellhop we are running the thread below */
    @Override //same function as previously explained
    public void run() 
	{
        try 
		{
            while (true) 
			{
                // Wait until a customer requests for bag help
				// Once the call has been made, assign that custoemr to a unique bellhop who can take his baggage
                ProjectTwo.customerService.acquire();
                ProjectTwo.mutexThree.acquireUninterruptibly(); // no interrupts when mutex is being held
                Guest hotelguest = ProjectTwo.bQueue.remove(); // removes that customer from baggage queue after
				// he or she recieves help

                // Below we are telling the customer or providing information on the belllhop holding their baggage
                ProjectTwo.bellHopCare[hotelguest.gNumber] = bellHopNumber;
                ProjectTwo.mutexThree.release(); // signal call

                // Here we are indicating to the guest that the bags have been delivered
                System.out.println("Bellhop " + bellHopNumber + " receives bags from guest " + hotelguest.gNumber);
                ProjectTwo.baggageService.release();

                // Wait call until guest enters the room
				// After that release the baggage
				// After the baggage has been recieved, then belhop is tipped and gets released.
                ProjectTwo.roomOccupied[hotelguest.gNumber].acquire();
                System.out.println("Bellhop " + bellHopNumber + " delivers bags to guest " + hotelguest.gNumber);
                ProjectTwo.baggage.release();
                ProjectTwo.bellHop.release();
            }
        }
        catch(Exception e) //catch any exceptions that may be thrown
			{
				e.printStackTrace();
			}
		// Java finally block is always executed whether exception is handled or not. Java finally block follows try or catch block.	
        finally {}
    }
}

// Class: Guest
/* Thread requirements for class: 25 guests visit the hotel (1 thread per guest created at start of simulation).
	Each guest has a random number of bags (0-5).
	A guest must check in to the hotel at the front desk.
	Upon check in, a guest gets a room number from the front desk employee.
	A guest with more than 2 bags requires a bellhop.
	The guest enters the assigned room.
	Receives bags from bellhop and gives tip (if more than 2 bags).
	Retires for the evening. */

class Guest implements Runnable
{
	//Variables
    public Thread hotelguest;
	public static int gJoins = 0;
	public ProjectTwo  hotelSimulation;
	
    public int    gNumber; // guest number
    public int    bNumber; // baggage number
    public int    rNumber; // room number
	
	// Constant static variables - guests can have 0 bags or up to a max of 5 bags
    public static final int MAXBAGS = 5;
    public static final int MINBAGS = 0;


    // Below we are intializing the variables for every instance of Guest
    public Guest(int number, ProjectTwo hotelSimulation) 
	{
        Random rand = new Random();
        this.hotelSimulation = hotelSimulation;
        gNumber = number;
        bNumber = rand.nextInt(MAXBAGS - MINBAGS + 1);

        System.out.println("Guest "  + gNumber + " created"); //create guests
        hotelguest = new Thread(this);
        hotelguest.start();
    }

    // For every given instance of guest we are running the thread below 
    @Override
    public void run() 
	{
        try {
            System.out.println("Guest " + gNumber + " enters the hotel with " + bNumber + " bags");

            // Critical section code
			// Adds the guest to the queue so he or she can get help from the employee available
            ProjectTwo.mutexOne.acquire();
            ProjectTwo.gQueue.add(this);
            ProjectTwo.mutexOne.release();

            // Guest waits for an available employee and gets the guests gets allocated to a unique room number
            ProjectTwo.employee.acquire();
            ProjectTwo.availableCustomer.release();
            ProjectTwo.serviceDone[gNumber].acquire();
            ProjectTwo.allocatedRoom.acquire();
            System.out.println("Guest " + gNumber + " receives room key for room " + rNumber + " from employee " + ProjectTwo.employeeCare[gNumber]);

            // Guest got help. Leaving from front desk so we release the semaphore
            ProjectTwo.servedEmployee.release();


            // If a guest have more than 2 bags, then they can get a bell hop
            if (bNumber > 2) 
			{
                // Guests waits for the bellhop after they indicate that they need help
                ProjectTwo.bellHop.acquire();
                System.out.println("Guest " + gNumber + " requests help with bags");
                ProjectTwo.bQueue.add(this); //adds to the baggage due
                ProjectTwo.customerService.release();

                // Guest goes to his or her allocated room
                ProjectTwo.baggageService.acquire();
                System.out.println("Guest " + gNumber + " enters room " + rNumber);
                ProjectTwo.roomOccupied[gNumber].release();

                // Guest waits for the bellhop to bring his or her baggage. At this point room is already occupied so we released that semaphore
                ProjectTwo.baggage.acquire(); 
                System.out.println("Guest " + gNumber + " receives bags from bellhop " + ProjectTwo.bellHopCare[gNumber] + " and gives tip");

            }
            // If guest have 2 or less bags, they directly go the room and retire
            else 
			{
                ProjectTwo.roomOccupied[gNumber].release(); 
                System.out.println("Guest " + gNumber + " enters room " + rNumber);
            }

            // Guest retires for the evening
            System.out.println("Guest " + gNumber + " retires for the evening");
        }
		
        catch(Exception e) //catch any exception
		{
            e.printStackTrace();
        }
        finally 
		{
            // Below we are making sure that guests are joined after they have retired. 
            try 
			{
                ProjectTwo.addedGuests();
                System.out.println("Guest " + gNumber + " joined");
                hotelguest.join();       
            } 
            catch (InterruptedException e) //catch any exception thrown above
				{
					e.printStackTrace();
				}
        }
    }
}