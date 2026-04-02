import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { User, MapPin, Bell, Shield, LogOut, Camera, Save, Loader2 } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { Avatar, Alert, SectionHeader } from '../../components/common/index';
import { api } from '../../utils/api';

export const SettingsPage = () => {
  const { user, updateUser, logout } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('profile');
  const [loading, setLoading] = useState(true);
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingNotifications, setSavingNotifications] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [alertState, setAlertState] = useState(null);

  const [form, setForm] = useState({
    name: user?.name || '',
    email: user?.email || '',
    phone: '',
    location: user?.location || '',
  });

  const [notifications, setNotifications] = useState({
    bookingUpdates: true,
    chatMessages: true,
    promotions: false,
    sms: true,
  });

  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmNewPassword: '',
  });

  const getApiErrorMessage = (err, fallback) => {
    const raw = err?.response?.data;

    if (typeof raw === 'string' && raw.trim()) return raw;
    if (raw && typeof raw === 'object') {
      if (typeof raw.message === 'string' && raw.message.trim()) return raw.message;
      if (typeof raw.error === 'string' && raw.error.trim()) return raw.error;
      if (typeof raw.status === 'number') return `${fallback} (HTTP ${raw.status})`;
    }

    return fallback;
  };

  const roleLabel = useMemo(() => {
    const role = (user?.role || '').toLowerCase();
    if (!role) return 'Customer';
    return role.charAt(0).toUpperCase() + role.slice(1);
  }, [user?.role]);

  const memberSince = useMemo(() => {
    if (!user?.createdAt) return '2024';
    const date = new Date(user.createdAt);
    return Number.isNaN(date.getTime()) ? '2024' : `${date.getFullYear()}`;
  }, [user?.createdAt]);

  useEffect(() => {
    let isMounted = true;

    const loadSettings = async () => {
      try {
        setLoading(true);
        const { data } = await api.get('/users/me');

        if (!isMounted) return;

        setForm({
          name: data?.name || '',
          email: data?.email || '',
          phone: data?.phoneNumber || '',
          location: data?.location || '',
        });

        setNotifications({
          bookingUpdates: !!data?.bookingUpdates,
          chatMessages: !!data?.chatMessages,
          promotions: !!data?.promotions,
          sms: !!data?.sms,
        });
      } catch (err) {
        if (!isMounted) return;
        const status = err?.response?.status;
        const message = getApiErrorMessage(err, 'Failed to load settings from server.');

        if (status === 401 || status === 403 || message.toLowerCase().includes('user not found')) {
          logout();
          navigate('/login', { replace: true });
          return;
        }

        setAlertState({ type: 'error', message });
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    loadSettings();

    return () => {
      isMounted = false;
    };
  }, [logout, navigate]);

  const clearAlertLater = () => {
    setTimeout(() => setAlertState(null), 2500);
  };

  const handleSaveProfile = async () => {
    try {
      setSavingProfile(true);
      const payload = {
        name: form.name,
        phoneNumber: form.phone,
        location: form.location,
      };

      const { data } = await api.put('/users/me/profile', payload);
      setForm((prev) => ({
        ...prev,
        name: data?.name || prev.name,
        phone: data?.phoneNumber || prev.phone,
        location: data?.location || prev.location,
      }));

      updateUser({
        name: data?.name || form.name,
        location: data?.location || form.location,
      });

      setAlertState({ type: 'success', message: 'Profile updated successfully.' });
      clearAlertLater();
    } catch (err) {
      const message = getApiErrorMessage(err, 'Failed to update profile.');
      setAlertState({ type: 'error', message });
      clearAlertLater();
    } finally {
      setSavingProfile(false);
    }
  };

  const handleSaveNotifications = async () => {
    try {
      setSavingNotifications(true);
      await api.put('/users/me/notifications', notifications);
      setAlertState({ type: 'success', message: 'Notification preferences saved.' });
      clearAlertLater();
    } catch (err) {
      const message = getApiErrorMessage(err, 'Failed to save notification preferences.');
      setAlertState({ type: 'error', message });
      clearAlertLater();
    } finally {
      setSavingNotifications(false);
    }
  };

  const handleUpdatePassword = async () => {
    if (!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmNewPassword) {
      setAlertState({ type: 'error', message: 'Please fill all password fields.' });
      clearAlertLater();
      return;
    }

    if (passwordForm.newPassword.length < 6) {
      setAlertState({ type: 'error', message: 'New password must be at least 6 characters.' });
      clearAlertLater();
      return;
    }

    if (passwordForm.newPassword !== passwordForm.confirmNewPassword) {
      setAlertState({ type: 'error', message: 'New password and confirm password do not match.' });
      clearAlertLater();
      return;
    }

    try {
      setSavingPassword(true);
      await api.put('/users/me/password', passwordForm);
      setPasswordForm({ currentPassword: '', newPassword: '', confirmNewPassword: '' });
      setAlertState({ type: 'success', message: 'Password updated successfully.' });
      clearAlertLater();
    } catch (err) {
      const message = getApiErrorMessage(err, 'Failed to update password.');
      setAlertState({ type: 'error', message });
      clearAlertLater();
    } finally {
      setSavingPassword(false);
    }
  };

  const tabs = [
    { id: 'profile', label: 'Profile', icon: User },
    { id: 'notifications', label: 'Notifications', icon: Bell },
    { id: 'security', label: 'Security', icon: Shield },
  ];

  return (
    <div className="space-y-6 animate-fade-in">
      <SectionHeader title="Settings" subtitle="Manage your account preferences" />

      <div className="grid lg:grid-cols-4 gap-6">
        {/* Sidebar */}
        <div className="lg:col-span-1">
          <div className="bg-dark-800 border border-dark-700 rounded-2xl p-4 space-y-1">
            {tabs.map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`sidebar-link w-full ${activeTab === tab.id ? 'active' : ''}`}
              >
                <tab.icon className="w-5 h-5" />
                {tab.label}
              </button>
            ))}
            <div className="border-t border-dark-700 pt-2 mt-2">
              <button
                onClick={logout}
                className="sidebar-link w-full text-red-400 hover:text-red-300 hover:bg-red-500/10"
              >
                <LogOut className="w-5 h-5" />
                Sign Out
              </button>
            </div>
          </div>
        </div>

        {/* Content */}
        <div className="lg:col-span-3">
          {alertState && <Alert type={alertState.type} message={alertState.message} />}

          {loading ? (
            <div className="bg-dark-800 border border-dark-700 rounded-2xl p-8 flex items-center justify-center gap-3 text-dark-300">
              <Loader2 className="w-5 h-5 animate-spin" />
              Loading settings...
            </div>
          ) : (
            <>
          {activeTab === 'profile' && (
            <div className="bg-dark-800 border border-dark-700 rounded-2xl p-6 space-y-6">
              {/* Avatar */}
              <div className="flex items-center gap-5">
                <div className="relative">
                  <Avatar name={form.name} size="xl" />
                  <button className="absolute bottom-0 right-0 w-8 h-8 bg-brand-500/80 rounded-full flex items-center justify-center cursor-not-allowed" disabled>
                    <Camera className="w-4 h-4 text-white" />
                  </button>
                </div>
                <div>
                  <h3 className="font-semibold text-white text-lg">{form.name}</h3>
                  <p className="text-dark-400 text-sm capitalize">{roleLabel}</p>
                  <p className="text-dark-500 text-xs mt-1">Member since {memberSince}</p>
                </div>
              </div>

              {/* Form */}
              <div className="grid sm:grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-dark-300 mb-1.5 block">Full Name</label>
                  <input type="text" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} className="input-field" />
                </div>
                <div>
                  <label className="text-sm font-medium text-dark-300 mb-1.5 block">Email Address</label>
                  <input type="email" value={form.email} readOnly className="input-field opacity-70 cursor-not-allowed" />
                </div>
                <div>
                  <label className="text-sm font-medium text-dark-300 mb-1.5 block">Phone Number</label>
                  <input type="tel" placeholder="+91 9876543210" value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} className="input-field" />
                </div>
                <div>
                  <label className="text-sm font-medium text-dark-300 mb-1.5 block flex items-center gap-1">
                    <MapPin className="w-3.5 h-3.5" /> Location
                  </label>
                  <input type="text" placeholder="Your area" value={form.location} onChange={e => setForm({ ...form, location: e.target.value })} className="input-field" />
                </div>
              </div>
              <button onClick={handleSaveProfile} disabled={savingProfile} className="btn-primary flex items-center gap-2 disabled:opacity-60">
                {savingProfile ? (
                  <><Loader2 className="w-4 h-4 animate-spin" /> Saving...</>
                ) : (
                  <><Save className="w-4 h-4" /> Save Changes</>
                )}
              </button>
            </div>
          )}

          {activeTab === 'notifications' && (
            <div className="bg-dark-800 border border-dark-700 rounded-2xl p-6 space-y-5">
              <h3 className="font-display font-semibold text-lg text-white">Notification Preferences</h3>
              {[
                { key: 'bookingUpdates', label: 'Booking Updates', desc: 'Get notified about booking status changes' },
                { key: 'chatMessages', label: 'Chat Messages', desc: 'Receive notifications for new messages' },
                { key: 'promotions', label: 'Promotions & Offers', desc: 'Receive special offers and discounts' },
                { key: 'sms', label: 'SMS Notifications', desc: 'Receive important updates via SMS' },
              ].map(item => (
                <div key={item.key} className="relative py-3 pr-16 border-b border-dark-700 last:border-0 min-h-[72px] flex items-center">
                  <div className="min-w-0">
                    <p className="font-medium text-white text-sm">{item.label}</p>
                    <p className="text-dark-400 text-xs mt-0.5">{item.desc}</p>
                  </div>
                  <button
                    onClick={() => setNotifications({ ...notifications, [item.key]: !notifications[item.key] })}
                    className={`absolute right-0 top-1/2 -translate-y-1/2 w-12 h-6 rounded-full transition-colors ${notifications[item.key] ? 'bg-brand-500' : 'bg-dark-600'}`}
                  >
                    <span
                      className="absolute top-1 w-4 h-4 bg-white rounded-full transition-all duration-200"
                      style={{ left: notifications[item.key] ? '1.75rem' : '0.25rem' }}
                    />
                  </button>
                </div>
              ))}
              <button
                onClick={handleSaveNotifications}
                disabled={savingNotifications}
                className="btn-primary flex items-center gap-2 disabled:opacity-60"
              >
                {savingNotifications ? (
                  <><Loader2 className="w-4 h-4 animate-spin" /> Saving...</>
                ) : (
                  <><Save className="w-4 h-4" /> Save Preferences</>
                )}
              </button>
            </div>
          )}

          {activeTab === 'security' && (
            <div className="bg-dark-800 border border-dark-700 rounded-2xl p-6 space-y-5">
              <h3 className="font-display font-semibold text-lg text-white">Security Settings</h3>
              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium text-dark-300 mb-1.5 block">Current Password</label>
                  <input
                    type="password"
                    placeholder="••••••••"
                    className="input-field"
                    value={passwordForm.currentPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
                  />
                </div>
                <div>
                  <label className="text-sm font-medium text-dark-300 mb-1.5 block">New Password</label>
                  <input
                    type="password"
                    placeholder="••••••••"
                    className="input-field"
                    value={passwordForm.newPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                  />
                </div>
                <div>
                  <label className="text-sm font-medium text-dark-300 mb-1.5 block">Confirm New Password</label>
                  <input
                    type="password"
                    placeholder="••••••••"
                    className="input-field"
                    value={passwordForm.confirmNewPassword}
                    onChange={(e) => setPasswordForm({ ...passwordForm, confirmNewPassword: e.target.value })}
                  />
                </div>
                <button onClick={handleUpdatePassword} disabled={savingPassword} className="btn-primary flex items-center gap-2 disabled:opacity-60">
                  {savingPassword ? (
                    <><Loader2 className="w-4 h-4 animate-spin" /> Updating...</>
                  ) : (
                    'Update Password'
                  )}
                </button>
              </div>
              <div className="mt-6 pt-6 border-t border-dark-700">
                <h4 className="font-semibold text-white mb-4">Active Sessions</h4>
                <div className="space-y-3">
                  {[
                    { device: 'Chrome on Windows', location: 'Bengaluru, IN', current: true },
                    { device: 'iPhone 14', location: 'Bengaluru, IN', current: false },
                  ].map((session, i) => (
                    <div key={i} className="flex items-center justify-between p-3 bg-dark-900/50 rounded-xl">
                      <div>
                        <p className="text-sm font-medium text-white">{session.device}</p>
                        <p className="text-xs text-dark-400">{session.location}</p>
                      </div>
                      {session.current ? (
                        <span className="text-xs text-green-400 font-medium">Current</span>
                      ) : (
                        <button
                          className="text-xs text-dark-500 cursor-not-allowed"
                          disabled
                          title="Session revoke will be enabled with server-side session tracking"
                        >
                          Revoke
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};
