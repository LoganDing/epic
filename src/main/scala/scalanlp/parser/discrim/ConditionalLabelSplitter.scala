package scalanlp.parser
package discrim

import scalanlp.parser.projections.ProjectionIndexer
import splitting.StateSplitting
import scalanlp.util.Index

/**
 * Returns a new set of one-step splits as a ProjectionIndexer, as well as sibling splits
 * @author dlwh
 */
class ConditionalLabelSplitter[L](oneStepProjections: ProjectionIndexer[L,L],
                                  split: L=>Seq[L],
                                  fracToSplit: Double = 0.5) {
  def splitLabels[W](builder: ChartBuilder[ParseChart,L,W], trees: IndexedSeq[TreeInstance[Seq[L],W]]) = {
    val labelsToUnsplit = StateSplitting.determineLabelsToUnsplit(oneStepProjections, builder, trees, 1 - fracToSplit)
    val newCoarseLabels = Index(oneStepProjections.fineIndex.map { l =>
      if(labelsToUnsplit(oneStepProjections.project(l))) oneStepProjections.project(l)
      else l
    })
    val newFineLabels = Index(newCoarseLabels.flatMap(split))
    println(labelsToUnsplit)

    val oneStep = ProjectionIndexer.fromSplitter(oneStepProjections.fineIndex, newFineLabels, {(l:L) => if(labelsToUnsplit(oneStepProjections.project(l))) Seq(l) else split(l)} )
    val sibProj = ProjectionIndexer.fromSplitter(newCoarseLabels, newFineLabels, split)
    (oneStep,sibProj)
  }

}