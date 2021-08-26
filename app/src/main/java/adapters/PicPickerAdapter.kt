package adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.dimitrisligi.findtheanimals.R
import models.BoardSize
import kotlin.math.min

class PicPickerAdapter(val context: Context, val listOfPics: List<Uri>, val boardSize: BoardSize): RecyclerView.Adapter<PicPickerAdapter.ViewHolder> (){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PicPickerAdapter.ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.image_card_picker,parent,false)
        val imageCardWidth = parent.width / boardSize.getWidth()
        val imageCardHeight = parent.height / boardSize.getHeight()
        val imageCardLenght = min(imageCardHeight,imageCardWidth)
        val layoutParams = v.findViewById<ImageView>(R.id.ivCustomImage).layoutParams
        layoutParams.width = imageCardLenght
        layoutParams.height = imageCardLenght


        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: PicPickerAdapter.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int = boardSize.getPairs()

    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){


    }
}