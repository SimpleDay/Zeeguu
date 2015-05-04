package ch.unibe.scg.zeeguu.Wordlist_Fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import ch.unibe.scg.zeeguu.Data.IO;
import ch.unibe.scg.zeeguu.R;

/**
 * Zeeguu Application
 * Created by Pascal on 24/01/15.
 */
public class WordlistHeader implements IO {
    private final String name;
    private ArrayList<Item> children;
    private boolean groupOpen;

    public WordlistHeader(String name) {
        this.name = name;
        this.children = new ArrayList<>();
        groupOpen = false;
    }

    public String getName() {
        return name;
    }

    public void addChild(Item item) {
        children.add(item);
    }

    public Item removeChild(int position) {
        return children.remove(position);
    }

    public Item getChild(int position) {
        return children.get(position);
    }

    public int getChildrenSize() {
        return children.size();
    }

    public long getItemId(int position) {
        return getChild(position).getItemId();
    }

    public View getView(LayoutInflater inflater, View convertView) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_header, null);
            holder = new ViewHolder();

            holder.header_title = (TextView) convertView.findViewById(R.id.txtHeader);
            holder.group_status = (ImageView) convertView.findViewById(R.id.ic_action_group_status);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.header_title.setText(name);

        if (groupOpen)
            holder.group_status.setImageResource(R.drawable.ic_action_close_list_holo_light);
        else
            holder.group_status.setImageResource(R.drawable.ic_action_open_list_holo_light);

        return convertView;
    }

    public View getChildView(int childPosition, LayoutInflater inflater, View convertView) {
        return children.get(childPosition).getView(inflater, convertView);
    }

    public void setGroupOpen(boolean groupOpen) {
        this.groupOpen = groupOpen;
    }


    //// loading and writing my words from and to memory, IO interface  ////

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(children.size());
        for (Item r : children) {
            if (r.getItemId() != 0) {
                out.writeLong(r.getItemId());

                WordlistItem w = (WordlistItem) r;
                //native word saving
                out.writeInt(w.getNativeWord().length());
                out.writeChars(w.getNativeWord());
                //translation word saving
                out.writeInt(w.getTranslationedWord().length());
                out.writeChars(w.getTranslationedWord());
                //context saving
                out.writeInt(w.getContext().length());
                out.writeChars(w.getContext());
                //fromLanguage saving
                out.writeInt(w.getFromLanguage().length());
                out.writeChars(w.getFromLanguage());
                //toLanguage saving
                out.writeInt(w.getToLanguage().length());
                out.writeChars(w.getToLanguage());
            } else {
                out.writeLong(0);

                //saving name of info header
                WordlistInfoHeader w = (WordlistInfoHeader) r;
                out.writeInt(w.getName().length());
                out.writeChars(w.getName());
            }
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        int size = in.readInt();
        children = new ArrayList<Item>(size);
        for (int i = 0; i < size; i++) {
            //read all entries from the group and add it to the list
            long id = in.readInt();
            if (id != 0) {
                //load native word
                byte[] nativeWordData = new byte[in.readInt()];
                in.readFully(nativeWordData);
                //load translated word
                byte[] translatedWordData = new byte[in.readInt()];
                in.readFully(translatedWordData);
                //load context
                byte[] contextData = new byte[in.readInt()];
                in.readFully(contextData);
                //load from language
                byte[] fromLanguageData = new byte[in.readInt()];
                in.readFully(fromLanguageData);
                //load to language
                byte[] toLanguageData = new byte[in.readInt()];
                in.readFully(toLanguageData);
                //add wordlist item to the list
                children.add(new WordlistItem(id, new String(nativeWordData, "UTF-8"), new String(translatedWordData, "UTF-8"),
                        new String(contextData, "UTF-8"), new String(fromLanguageData, "UTF-8"), new String(toLanguageData, "UTF-8")));
            } else {
                byte[] nameData = new byte[in.readInt()];
                in.readFully(nameData);
                children.add(new WordlistInfoHeader(new String(nameData, "UTF-8")));
            }
        }
    }

    //// search all words in the WordListHeader for a valid translation ////

    public WordlistItem checkWordlistForTranslation(String input, String inputLanguage, String outputLanguage) {
        for (Item i : children) {
            WordlistItem result = i.isTranslation(input, inputLanguage, outputLanguage);
            if (result != null)
                return result;
        }
        return null;
    }

    //// View holder for the list elements so that they can be reused ////

    static class ViewHolder {
        TextView header_title;
        ImageView group_status;
    }
}
