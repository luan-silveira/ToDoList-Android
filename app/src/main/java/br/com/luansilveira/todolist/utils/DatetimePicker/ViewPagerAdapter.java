package br.com.luansilveira.todolist.utils.DatetimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private String[] tabs = {"Data", "Horário"};
    private TabDatePicker tabDatePicker;
    private TabTimePicker tabTimePicker;

    public ViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return this.getTabDatePicker();
            case 1:
                return this.getTabTimePicker();
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return this.tabs[position];
    }

    @Override
    public int getCount() {
        return 2;
    }

    private TabDatePicker getTabDatePicker() {
        return (this.tabDatePicker == null) ? (this.tabDatePicker = new TabDatePicker()) : this.tabDatePicker;
    }

    private TabTimePicker getTabTimePicker() {
        return (this.tabTimePicker == null) ? (this.tabTimePicker = new TabTimePicker()) : this.tabTimePicker;
    }
}
