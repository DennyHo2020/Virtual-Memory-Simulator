/**
 * PTE
 * 
 * PTE is a page table entry class that keeps track of
 * the dirty, reference, valid bits plus the frame number
 * and page number.
 * @author DennyHo
 *
 */
public class PTE {
	private boolean dirty;
	private boolean ref;
	private boolean valid;
	private int frameNumber;
	private int pageNumber;
	
	public PTE() {
		this.dirty = false;
		this.ref = true;
		this.valid = false;
		this.setFrameNumber(-1); // -1 to indicate that it has not been set
		this.setPageNumber(-1);  // -1 to indicate that it has not been set
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isRef() {
		return ref;
	}

	public void setRef(boolean ref) {
		this.ref = ref;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getFrameNumber() {
		return frameNumber;
	}

	public void setFrameNumber(int frameNumber) {
		this.frameNumber = frameNumber;
	}
	
}