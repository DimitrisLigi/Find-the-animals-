package adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.dimitrisligi.findtheanimals.R
import models.BoardSize
import kotlin.math.min

class MemoryBoardAdapter(private val context: Context, private val boardSize: BoardSize) :
    RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>(){


    companion object{
        private const val MARGIN_SIZE = 20
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        //Figuring out the size of the adapter
        val cardWidth = parent.width / boardSize.getWidth() - ( 2 * MARGIN_SIZE )
        val cardHeight = parent.height / boardSize.getHeight() - ( 2 * MARGIN_SIZE )
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

        private val imageButton: ImageButton = view.findViewById(R.id.imageButton)

        fun bind(position: Int){
            imageButton.setOnClickListener {
                Toast.makeText(this@MemoryBoardAdapter.context,
                    "I am at position $position",Toast.LENGTH_SHORT).show()
            }
        }
    }
}