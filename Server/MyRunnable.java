public class MyRunnable implements Runnable {
      private boolean isFull;
      public MyRunnable(boolean isFull) {
         this.isFull = isFull;
      }

      public void run() {
      }

	public boolean get() {
	return isFull;
}
   }