import pandas as pd
import os
import matplotlib.pyplot as plt


# get filenames in a folder
def get_csv_filenames(folder):
    filenames = []
    for filename in os.listdir(folder):
        if filename.endswith('.csv'):
            filenames.append(filename)
    return filenames


def remove_outliers(df):
    # Calculate Q1 (25th percentile) and Q3 (75th percentile)
    Q1 = df['ResponseTime'].quantile(0.25)
    Q3 = df['ResponseTime'].quantile(0.75)
    IQR = Q3 - Q1

    # Define the lower and upper bounds for outliers
    lower_bound = Q1 - 1.5 * IQR
    upper_bound = Q3 + 1.5 * IQR

    # Filter the DataFrame to remove outliers
    df_no_outliers = df[(df['ResponseTime'] >= lower_bound) & (df['ResponseTime'] <= upper_bound)]
    return df_no_outliers

# read one csv file into df
def read_csv_file(folder, filename):
    path = os.path.join(folder, filename)
    df = pd.read_csv(path)
    # df = df[10:]
    return df


# read all csv files in a folder
def read_csv_files(folder):
    filenames = get_csv_filenames(folder)
    # get length of filenames
    len = filenames.__len__()
    dfs = []
    for filename in filenames:
        df = read_csv_file(folder, filename)
        df['nodes']=len
        df = remove_outliers(df)
        dfs.append(df)
    return dfs

# for each df, calculate max value of index column, add one
# call this variable k
# group each consecutive k rows into one row
# for variable startTime, take min value in the k rows
# for variable endTime, take max value in the k rows
# for variable Response Time, take average
# ignore Batch,BatchOutput,Index columns
# sum addOps, deleteOps, queryOps, batchSize, timeSleptAfterThisBatch
# add another meanBatchSize column, which is batchSize/k
# add another meanTimeSleptAfterThisBatch column, which is timeSleptAfterThisBatch/k
# return a list of dfs that contains each row as summary of the k rows described above
def summarize(dfs):
    summarized_dfs = []
    
    for df in dfs:
        max_index = df['Index'].max()
        k = max_index + 1
        # add run column to each k group
#Index,StartTimestamp,EndTimestamp,ResponseTime,Batch,BatchOutput,writePercentage,addOps,deleteOps,queryOps,batchSize,timeSleptAfterThisBatch

        df['run'] = (df.index) // k
        # print("k is ", k, " df index is ", df.index)
        # print(df['run'])
        # print(df.head(10))
        summarized_df = df.groupby('run').agg({
            'StartTimestamp': 'min',
            'EndTimestamp': 'max',
            'ResponseTime': 'mean',
            'addOps': 'sum',
            'deleteOps': 'sum',
            'queryOps': 'sum',
            'batchSize': 'sum',
            'timeSleptAfterThisBatch': 'sum',
            'writePercentage':'mean',
            'nodes':'mean'
        })
        summarized_df['numBatches'] = k
        summarized_df['meanBatchSize'] = summarized_df['batchSize'] / k
        summarized_df['meanTimeSleptAfterThisBatch'] = summarized_df['timeSleptAfterThisBatch'] / k
        summarized_dfs.append(summarized_df)
    return summarized_dfs



# merging summarized dfs into one
# each entry has same run number, so we can merge on run number
# make start time to be min of all start times
# make end time to be max of all end times
# make Response Time to be average of all Response Times
# make addOps to be sum of all addOps
# make deleteOps to be sum of all deleteOps
# make queryOps to be sum of all queryOps
# make batchSize to be sum of all batchSize
# make timeSleptAfterThisBatch to be sum of all timeSleptAfterThisBatch
# make meanBatchSize to be average of all meanBatchSize
# make meanTimeSleptAfterThisBatch to be average of all meanTimeSleptAfterThisBatch
# return a single df
def merge_summarized_dfs(summarized_dfs):
    df = pd.concat(summarized_dfs)
    df = df.groupby('run').agg({
        'StartTimestamp': 'min',
        'EndTimestamp': 'max',
        'numBatches': 'sum',
        'ResponseTime': 'mean',
        'addOps': 'sum',
        'deleteOps': 'sum',
        'queryOps': 'sum',
        'batchSize': 'sum',
        'timeSleptAfterThisBatch': 'sum',
        'meanBatchSize': 'mean',
        'meanTimeSleptAfterThisBatch': 'mean',
        'writePercentage':'mean',
        'nodes':'mean'
    })
    return df


# for each run calculate frequency which is number of batches / total time
# return table (df) with run number and frequency vs response time
def calculate_frequency_vs_response_time(df):
    df['frequency'] = 1000*df['numBatches'] /                          \
                ((df['EndTimestamp'] - df['StartTimestamp']))
    return df[['frequency', 'ResponseTime']]


# for each run calculate response time vs write percentage
# return table (df) with run number and write percentage vs response time
def calculate_write_percentage_vs_response_time(df):
    df['calculatedWritePercentage'] = (df['addOps']+ df['deleteOps']) / (df['addOps']+ df['deleteOps'] + df['queryOps'])*100
    return df[['calculatedWritePercentage', 'ResponseTime']]


# calculate response time vs number of nodes
def calculate_nodes_vs_response_time(df):
    return df[['nodes', 'ResponseTime']]



# get all folder names in folder "experiments"
def get_folder_names(folder):
    folder_names = []
    for filename in os.listdir(folder):
        if os.path.isdir(os.path.join(folder, filename)):
            folder_names.append(filename)
    return folder_names


# dfs = read_csv_files('experiments/experiment1_w0_sparse')
# summarized_dfs = summarize(dfs)
# merged_df = merge_summarized_dfs(summarized_dfs)
# print(merged_df['ResponseTime'])




# dfs_inc = read_csv_files('experiments_inc/experiment1_w0_sparse')
# summarized_dfs_inc = summarize(dfs_inc)
# merged_df_inc = merge_summarized_dfs(summarized_dfs_inc)
# print(merged_df_inc['ResponseTime'])


def do_analysis(log_folder, results_folder):
    experiments_folders = get_folder_names(log_folder)

    wp_vs_rt_table = pd.DataFrame()
    f_vs_rt_table = pd.DataFrame()
    c_vs_rt_table = pd.DataFrame()

    for folder in experiments_folders:
        folder = os.path.join(log_folder, folder)
        print("Processing folder ", folder)
        dfs = read_csv_files(folder)
        summarized_dfs = summarize(dfs)
        merged_df = merge_summarized_dfs(summarized_dfs)
        # if folders contains write
        if(folder.find("write") != -1):
            #append to write_percentage_vs_response_time_table
            wp_vs_rt_table = wp_vs_rt_table._append(calculate_write_percentage_vs_response_time(merged_df))
        # if folders contains clients
        if(folder.find("sleep") != -1):
            #append to frequency_vs_response_time_table
            f_vs_rt_table = f_vs_rt_table._append(calculate_frequency_vs_response_time(merged_df))
        
        if(folder.find("clients") != -1):
            #append to nodes_vs_response_time_table
            c_vs_rt_table = c_vs_rt_table._append(calculate_nodes_vs_response_time(merged_df))

    #sort the dataframe by the first column
    wp_vs_rt_table = wp_vs_rt_table.sort_values(by='calculatedWritePercentage')
    f_vs_rt_table = f_vs_rt_table.sort_values(by='frequency')
    c_vs_rt_table = c_vs_rt_table.sort_values(by='nodes')

    # create results directory if it doesn't exist
    if not os.path.exists(results_folder):
        os.makedirs(results_folder)

    # save each table to csv, overwrite
    wp_vs_rt_table.to_csv(results_folder + '/write_percentage_vs_response_time.csv', index=False)
    f_vs_rt_table.to_csv(results_folder + '/frequency_vs_response_time.csv', index=False)
    c_vs_rt_table.to_csv(results_folder + '/nodes_vs_response_time.csv', index=False)

do_analysis('experiments', 'results2')
do_analysis('experiments_inc', 'results_inc2')


#plot each csv in folder and save plots to that folder
#in the plot make x axis as first column and y axis as second column




def plot_csv_files(folder):
    filenames = get_csv_filenames(folder)
    for filename in filenames:
        path = os.path.join(folder, filename)
        df = pd.read_csv(path)
        # get column names
        columns = df.columns
        df.plot(x=columns[0], y=columns[1], kind='line')
        plot_path = os.path.join(folder, filename.replace('.csv', '.png'))
        plt.savefig(plot_path)
        plt.close()

plot_csv_files('results2')
plot_csv_files('results_inc2')



def plot_csv_files(folder1, folder2):
    filenames1 = get_csv_filenames(folder1)
    filenames2 = get_csv_filenames(folder2)
    
    # Create a new directory for comparison graphs
    comparison_folder = 'comparison_graphs'
    if not os.path.exists(comparison_folder):
        os.makedirs(comparison_folder)

    for filename1, filename2 in zip(filenames1, filenames2):
        path1 = os.path.join(folder1, filename1)
        path2 = os.path.join(folder2, filename2)
        
        df1 = pd.read_csv(path1)
        df2 = pd.read_csv(path2)
        
        # get column names
        columns1 = df1.columns
        columns2 = df2.columns
        
        plt.figure()
        plt.plot(df1[columns1[0]], df1[columns1[1]], label='non_incremental')
        plt.plot(df2[columns2[0]], df2[columns2[1]], label='incremental')
        
        plt.xlabel(columns1[0])  # or columns2[0] if they have the same meaning
        plt.ylabel(columns1[1])  # or columns2[1] if they have the same meaning
        plt.legend()
        
        plot_path = os.path.join(comparison_folder, filename1.replace('.csv', '.png'))
        plt.savefig(plot_path)
        plt.close()

plot_csv_files('results2', 'results_inc2')

# import random

# with open('pairs.txt', 'w') as f:
 
#     for i in range(8000): 
#         x = random.randint(1, 200)
#         y = random.randint(1, 200)
#         f.write(f'{x} {y}\n')
#     f.write('S')