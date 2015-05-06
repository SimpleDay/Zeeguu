package ch.unibe.scg.zeeguu.MyWords_Fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import ch.unibe.scg.zeeguu.Data.IO;
import ch.unibe.scg.zeeguu.R;

/**
 * Zeeguu Application
 * Created by Pascal on 24/01/15.
 */
public class MyWordsHeader implements IO {
    private final String name;
    private ArrayList<Item> children;
    private boolean groupOpen;

    public MyWordsHeader(String name) {
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
    public void write(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(children.size());
        bufferedWriter.newLine();

        for (Item r : children) {
            if (r.getItemId() != 0) {
                bufferedWriter.write(Long.toString(r.getItemId()));
                bufferedWriter.newLine();

                MyWordsItem w = (MyWordsItem) r;
                //native word saving
                bufferedWriter.write(w.getNativeWord());
                bufferedWriter.newLine();
                //translation word saving
                bufferedWriter.write(w.getTranslationedWord());
                bufferedWriter.newLine();
                //context saving
                bufferedWriter.write(w.getContext());
                bufferedWriter.newLine();
                //fromLanguage saving
                bufferedWriter.write(w.getFromLanguage());
                bufferedWriter.newLine();
                //toLanguage saving
                bufferedWriter.write(w.getToLanguage());
                bufferedWriter.newLine();
            } else {
                bufferedWriter.write(0);
                bufferedWriter.newLine();

                //saving name of info header
                MyWordsInfoHeader w = (MyWordsInfoHeader) r;
                bufferedWriter.write(w.getName());
                bufferedWriter.newLine();
            }
        }
    }

    @Override
    public void read(BufferedReader bufferedReader) throws IOException {
        int size = Integer.parseInt(bufferedReader.readLine().trim());
        for (int i = 0; i < size; i++) {
            //read all entries from the group and add it to the list
            long id = Long.parseLong(bufferedReader.readLine().trim());
            if (id > 0) {
                //load native word
                String nativeWordData = bufferedReader.readLine();
                //load translated word
                String translatedWordData = bufferedReader.readLine();
                //load context
                String contextData = bufferedReader.readLine();
                //load from language
                String fromLanguageData = bufferedReader.readLine();
                //load to language
                String toLanguageData = bufferedReader.readLine();
                //add myword item to the list
                children.add(new MyWordsItem(id, nativeWordData, translatedWordData,
                        contextData, fromLanguageData, toLanguageData));
            } else {
                children.add(new MyWordsInfoHeader(bufferedReader.readLine()));
            }
        }
    }

    //// search all words in the MyWordsHeader for a valid translation ////

    public MyWordsItem checkMyWordsForTranslation(String input, String inputLanguage, String outputLanguage) {
        for (Item i : children) {
            MyWordsItem result = i.isTranslation(input, inputLanguage, outputLanguage);
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
