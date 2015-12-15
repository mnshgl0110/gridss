package au.edu.wehi.idsv;

import htsjdk.samtools.Cigar;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.TextCigarCodec;
import htsjdk.variant.variantcontext.VariantContext;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

import au.edu.wehi.idsv.vcf.VcfAttributes;
import au.edu.wehi.idsv.vcf.VcfSvConstants;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Ordering;

/**
 * VCF Breakend record
 * see Section 5.4.9 of http://samtools.github.io/hts-specs/VCFv4.2.pdf for details of breakends
 * @author Daniel Cameron
 *
 */
public class VariantContextDirectedEvidence extends IdsvVariantContext implements DirectedEvidence {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final VcfBreakendSummary breakend;
	//private static Log LOG = Log.getInstance(VariantContextDirectedBreakpoint.class);
	public VariantContextDirectedEvidence(ProcessingContext processContext, EvidenceSource source, VariantContext context) {
		super(processContext, source, context);
		this.breakend = new VcfBreakendSummary(processContext, context);
	}
	@Override
	public BreakendSummary getBreakendSummary() {
		if (breakend.location == null) throw new IllegalStateException(String.format("%s not a valid breakend", getID()));
		return breakend.location;
	}
	@Override
	public String getEvidenceID() {
		return getID();
	}
	@Override
	public byte[] getBreakendSequence() {
		return breakend.breakpointSequence.getBytes(StandardCharsets.US_ASCII);
	}
	public String getBreakpointSequenceString() {
		if (breakend.breakpointSequence == null) throw new IllegalStateException(String.format("%s not a valid breakend", getID()));
		return breakend.breakpointSequence;
	}
	@Override
	public byte[] getBreakendQuality() {
		return null;
	}
	@Override
	public byte[] getAnchorSequence() {
		return null;
	}
	@Override
	public byte[] getAnchorQuality() {
		return null;
	}
	@Override
	public boolean isValid() {
		return breakend.location != null;
	}
	/**
	 * Returns an iterator containing only the breakend variants from the given iterator
	 * @param context processing context
	 * @param variants input variants
	 * @return breakend variants
	 */
	public static Iterator<VariantContextDirectedEvidence> breakendIterator(final ProcessingContext context, final EvidenceSource source, final Iterator<VariantContext> variants) {
		 Iterator<IdsvVariantContext> itivc = Iterators.transform(variants, new Function<VariantContext, IdsvVariantContext>() {
			public IdsvVariantContext apply(VariantContext v) {
				return new IdsvVariantContextBuilder(context, v).source(source).make();
			}
		});
		 Iterator<VariantContextDirectedEvidence> itde = Iterators.filter(Iterators.filter(itivc, VariantContextDirectedEvidence.class), new Predicate<VariantContextDirectedEvidence>() {
			public boolean apply(VariantContextDirectedEvidence v) {
				return v.isValid();
			}
		});
		return itde;
	}
	public static Ordering<VariantContextDirectedEvidence> ByBreakendStartEnd = new Ordering<VariantContextDirectedEvidence>() {
		public int compare(VariantContextDirectedEvidence o1, VariantContextDirectedEvidence o2) {
			return BreakendSummary.ByStartEnd.compare(o1.getBreakendSummary(), o2.getBreakendSummary());
		  }
	};
	@Override
	public int getLocalMapq() {
		throw new IllegalArgumentException("NYI");
	}
	@Override
	public int getLocalBaseLength() {
		throw new IllegalArgumentException("NYI");
	}
	@Override
	public int getLocalMaxBaseQual() {
		throw new IllegalArgumentException("NYI");
	}
	@Override
	public int getLocalTotalBaseQual() {
		throw new IllegalArgumentException("NYI");
	}
	@Override
	public boolean isBreakendExact() {
		return !hasAttribute(VcfSvConstants.IMPRECISE_KEY);
	}
	@Override
	public float getBreakendQual() {
		return (float)getPhredScaledQual();
	}
	/**
	 * Converts to SAMRecord representation.
	 * @param header
	 * @return SAMRecord
	 */
	public SAMRecord asSamRecord(SAMFileHeader header) {
		BreakendSummary be = getBreakendSummary();
		SAMRecord r = new SAMRecord(header);
		Cigar cigar = new Cigar(be.getCigarRepresentation());
		if (hasAttribute(VcfAttributes.ANCHOR_CIGAR.attribute())) {
			cigar = TextCigarCodec.decode(getAttributeAsString(VcfAttributes.ANCHOR_CIGAR.attribute(), null));
		}
		r.setCigar(cigar);
		r.setReadName(getAttributeAsString(VcfSvConstants.BREAKEND_EVENT_ID_KEY, getID()));
		byte[] bases = new byte[cigar.getReadLength()];
		Arrays.fill(bases, (byte)'N');
		r.setReadBases(bases);
		r.setBaseQualities(SAMRecord.NULL_SEQUENCE);
		r.setReferenceIndex(be.referenceIndex);
		if (be.direction == BreakendDirection.Forward) {
			r.setReadNegativeStrandFlag(false);
			r.setAlignmentStart(be.end - cigar.getReadLength() + 1);
		} else {
			r.setReadNegativeStrandFlag(true);
			r.setAlignmentStart(getBreakendSummary().start);
		}
		if (be instanceof BreakpointSummary) {
			BreakendSummary rbe = ((BreakpointSummary)be).remoteBreakend();
			r.setReadPairedFlag(true);
			r.setMateNegativeStrandFlag(getBreakendSummary().direction == BreakendDirection.Backward);
			r.setMateReferenceIndex(rbe.referenceIndex);
			// incorrect if mate is FWD and has an ANCHOR_CIGAR defined
			r.setMateAlignmentStart(rbe.start);
			r.setMateUnmappedFlag(false);
			r.setFirstOfPairFlag(getID().endsWith("o"));
			r.setSecondOfPairFlag(getID().endsWith("h"));
		}
		return r;
	}
	public int getBreakendEvidenceCountAssembly() { return AttributeConverter.asInt(getAttribute(VcfAttributes.BREAKEND_ASSEMBLY_COUNT.attribute()), 0); }
	public int getBreakendEvidenceCountReadPair() { return getAttributeIntSum(VcfAttributes.BREAKEND_UNMAPPEDMATE_COUNT); }
	public int getBreakendEvidenceCountReadPair(int category) { return getAttributeIntOffset(VcfAttributes.BREAKEND_UNMAPPEDMATE_COUNT, category); } 
	public int getBreakendEvidenceCountSoftClip() { return getAttributeIntSum(VcfAttributes.BREAKEND_SOFTCLIP_COUNT); }
	public int getBreakendEvidenceCountSoftClip(int category) { return getAttributeIntOffset(VcfAttributes.BREAKEND_SOFTCLIP_COUNT, category); }
	public int getReferenceReadCount() { return getAttributeIntSum(VcfAttributes.REFERENCE_READ_COUNT); }
	public int getReferenceReadCount(int category) { return getAttributeIntOffset(VcfAttributes.REFERENCE_READ_COUNT, category); }	
	public int getReferenceReadPairCount() { return getAttributeIntSum(VcfAttributes.REFERENCE_READPAIR_COUNT); }
	public int getReferenceReadPairCount(int category) { return getAttributeIntOffset(VcfAttributes.REFERENCE_READPAIR_COUNT, category); }
}
