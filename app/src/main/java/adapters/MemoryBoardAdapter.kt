package adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.dimitrisligi.findtheanimals.R
import com.squareup.picasso.Picasso
import gameinterfaces.ClickCardListener
import models.BoardSize
import models.MemoryCard
import kotlin.math.min

class MemoryBoardAdapter(private val context: Context,
                         private val boardSize: BoardSize,
                         private val card: List<MemoryCard>,
                         private val clickCardListener: ClickCardListener) :
    RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>(){

    companion object{
        private const val MARGIN_SIZE =20
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        //Figuring out the size of the adapter
        val cardWidth = parent.width / boardSize.getWidth() - ( 2 * MARGIN_SIZE )
        val cardHeight = parent.height / boardSize.getHeight()- ( 2 * MARGIN_SIZE )

        //Taking the minimum length side.
        val cardSideLength = min(cardWidth,cardHeight)

        val v = LayoutInflater.from(context).inflate(R.layout.memory_card, parent,false)
        val layoutParams = v.findViewById<CardView>(R.id.cv_memory_card).layoutParams as ViewGroup.MarginLayoutParams

        layoutParams.width = cardSideLength
        layoutParams.height = cardSideLength
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = boardSize.numCads


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){

        //Declaring variables
        private val imageButton: ImageButton = view.findViewById(R.id.imageButton)


        fun bind(position: Int){

            //Making an instance of a memory card
            val memoryCard = card[position]

            /**
             * If our card is face up then we draw our identifier.
             * If out card is face down then we draw the default background icon
             */
            if(memoryCard.faceUp){
                if(memoryCard.imageUrl != null){
                    Picasso.get().load(memoryCard.imageUrl)
                        .placeholder(R.drawable.ic_loading_image).into(imageButton)
                }else{
                    imageButton.setImageResource(memoryCard.identifier)
                }
            }else{
                imageButton.setImageResource(R.drawable.ic_default_back)
            }


            //Decreasing the alpha if you have a match
            imageButton.alpha = if (memoryCard.isMatched) .4f else 1.0f

            //Making the imageButton grey if it is matched
            val colorStateList = if (memoryCard.isMatched) ContextCompat
                .getColorStateList(context,R.color.color_grey) else null

            ViewCompat.setBackgroundTintList(imageButton,colorStateList)

            imageButton.setOnClickListener {
                clickCardListener.onCardClicked(position)
            }
        }
    }
}