package auxs;
/*
 * 
 * Inicia no INDICE 1
 */
public class MaxHeap { 
	private int[] HeapIndex; 
    private double[] Heap; 
    private int size; 
    private int maxsize; 
  
    // Constructor to initialize an 
    // empty max heap with given maximum 
    // capacity. 
   
    public MaxHeap(int maxsize) 
    { 
        this.maxsize = maxsize; 
        this.size = 0; 
        HeapIndex = new int[this.maxsize + 1]; 
        HeapIndex[0] = Integer.MAX_VALUE; 
        Heap = new double[this.maxsize + 1]; 
        Heap[0] = Double.MAX_VALUE; 
    } 
  
    // Returns position of parent 
    private int parent(int pos) 
    { 
        return pos / 2; 
    } 
  
    // Below two functions return left and 
    // right children. 
    private int leftChild(int pos) 
    { 
        return (2 * pos); 
    } 
    private int rightChild(int pos) 
    { 
        return (2 * pos) + 1; 
    } 
  
    // Returns true of given node is leaf 
    private boolean isLeaf(int pos) 
    { 
        if (pos >= (size / 2) && pos <= size) { 
            return true; 
        } 
        return false; 
    } 
  
    private void swap(int fpos, int spos) 
    { 
        int tmp; 
        tmp = HeapIndex[fpos]; 
        HeapIndex[fpos] = HeapIndex[spos]; 
        HeapIndex[spos] = tmp; 
        
        double temp = Heap[fpos]; 
        Heap[fpos] = Heap[spos]; 
        Heap[spos] = temp; 
        
    } 
  
    // A recursive function to max heapify the given 
    // subtree. This function assumes that the left and 
    // right subtrees are already heapified, we only need 
    // to fix the root. 
    private void maxHeapify(int pos) 
    { 
        if (isLeaf(pos)) 
            return; 
  
        if (Heap[pos] < Heap[leftChild(pos)] ||  
            Heap[pos] < Heap[rightChild(pos)]) { 
  
            if (Heap[leftChild(pos)] > Heap[rightChild(pos)]) { 
                swap(pos, leftChild(pos)); 
                maxHeapify(leftChild(pos)); 
            } 
            else { 
                swap(pos, rightChild(pos)); 
                maxHeapify(rightChild(pos)); 
            } 
        } 
    } 
    
    private void minHeapify(int pos) 
    { 
        if (isLeaf(pos)) 
            return; 
  
        if (Heap[pos] > Heap[leftChild(pos)] ||  
            Heap[pos] > Heap[rightChild(pos)]) { 
  
            if (Heap[leftChild(pos)] < Heap[rightChild(pos)]) { 
                swap(pos, leftChild(pos)); 
                minHeapify(leftChild(pos)); 
            } 
            else { 
                swap(pos, rightChild(pos)); 
                minHeapify(rightChild(pos)); 
            } 
        } 
    } 
  
    // Inserts a new element to max heap 
    public void insertMax(int index, double value) 
    { 
        HeapIndex[size] = index; 
        Heap[size] = value;
        size++;
  
        // Traverse up and fix violated property 
        int current = size; 
        while (Heap[current] > Heap[parent(current)]) { 
            swap(current, parent(current)); 
            current = parent(current); 
        } 
    } 
    

  
  
    public void print() 
    { 
        for (int i = 1; i <= size / 2; i++) { 
            System.out.print(" PARENT : " + Heap[i] + " LEFT CHILD : " + 
                      Heap[2 * i] + " RIGHT CHILD :" + Heap[2 * i + 1]); 
            System.out.println(); 
        } 
    } 
  
    // Remove an element from max heap 
    private int extractMaxIndex() 
    { 
        int popped = HeapIndex[1]; 
        
   
        return popped; 
    } 
    private double extractMaxValue() 
    { 
        double popped = Heap[1]; 
        
        return popped; 
    }
    
    
    public void extractMax(Integer index, Double value) {
    	index = extractMaxIndex();
    	value = extractMaxValue();
    	HeapIndex[1] = HeapIndex[size]; 
    	Heap[1] = Heap[size--]; 
    	
    	maxHeapify(1);
    	
    	return;
    }
    
    
    public int peekIndex() {
    	return HeapIndex[1]; 
    }
    
    public double peekValue() {
    	return Heap[1]; 
    }


}